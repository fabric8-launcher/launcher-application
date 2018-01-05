/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.forge.generator.quickstart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.launcher.addon.BoosterCatalogFactory;
import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.BoosterCatalog;
import io.openshift.booster.catalog.Mission;
import io.openshift.booster.catalog.Runtime;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a single list of boosters to pick from
 */
public class ChooseBoosterStep implements UIWizardStep {
    private static final transient Logger LOG = LoggerFactory.getLogger(ChooseBoosterStep.class);

    @Inject
    private BoosterCatalogFactory catalogFactory;

    @Inject
    @WithAttributes(label = "Quickstart", required = true)
    private UISelectOne<BoosterDTO> quickstart;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext context = builder.getUIContext();
        BoosterCatalog catalog = catalogFactory.getCatalog(context);
        Collection<Booster> boosters = catalog.getBoosters();

        Map<String, BoosterDTO> map = new HashMap<>();
        boolean customBoosterCatalog = hasCustomBoosterCatalog(context);
        for (Booster booster : boosters) {
            if (customBoosterCatalog || ValidBoosters.validRhoarBooster(booster)) {
                // TODO lets filter out duplicate named boosters for now
                // as they break the combo box UX
                // once we move away from combo box we can return all versions of all boosters
                // with the same name though!
                String key = booster.getName();
                if (!map.containsKey(key)) {
                    map.put(key, new BoosterDTO(booster));
                }
            }
        }
        List<BoosterDTO> boosterList = new ArrayList<>(map.values());
        Collections.sort(boosterList);

        if (context.getProvider().isGUI()) {
            quickstart.setItemLabelConverter(BoosterDTO::getName);
        } else {
            quickstart.setItemLabelConverter(BoosterDTO::getId);
        }

        quickstart.setValueChoices(boosterList);

        if (!boosterList.isEmpty()) {
            quickstart.setDefaultValue(pickDefaultBooster(boosterList));
        }
        builder.add(quickstart);
    }

    /**
     * Returns true if there is a custom user speific booster catalog
     */
    private boolean hasCustomBoosterCatalog(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();
        return attributeMap.containsKey(AttributeMapKeys.CATALOG_GIT_REF) ||
                attributeMap.containsKey(AttributeMapKeys.CATALOG_GIT_REPOSITORY);
    }

    protected BoosterDTO pickDefaultBooster(List<BoosterDTO> boosterList) {
        for (BoosterDTO boosterDTO : boosterList) {
            if ("vertx-http-booster".equals(boosterDTO.getId())) {
                return boosterDTO;
            }
        }
        for (BoosterDTO boosterDTO : boosterList) {
            if (boosterDTO.getName().startsWith("Vert.x")) {
                return boosterDTO;
            }
        }
        return boosterList.get(0);
    }

    @Override
    public void validate(UIValidationContext context) {
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Quickstart")
                .description("Choose a quickstart")
                .category(Categories.create("Openshift.io"));
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        updateAttributes(uiContext);
        return null;
    }

    protected void updateAttributes(UIContext uiContext) {
        BoosterDTO boosterDTO = quickstart.getValue();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();
        if (boosterDTO != null) {
            Mission mission = boosterDTO.mission();
            Booster booster = boosterDTO.booster();
            Runtime runtime = boosterDTO.runtime();

            attributeMap.put(BoosterDTO.class, boosterDTO);
            attributeMap.put(Mission.class, mission);
            attributeMap.put(Booster.class, booster);
            attributeMap.put(Runtime.class, runtime);
        }
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        updateAttributes(uiContext);
        return Results.success();
    }

}
