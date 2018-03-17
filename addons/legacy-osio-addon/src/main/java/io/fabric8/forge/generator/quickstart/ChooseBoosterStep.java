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
import java.util.function.Predicate;

import javax.inject.Inject;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
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

/**
 * Provide a single list of boosters to pick from
 */
public class ChooseBoosterStep implements UIWizardStep {

    @Inject
    private RhoarBoosterCatalog catalog;

    @Inject
    @WithAttributes(label = "Quickstart", required = true)
    private UISelectOne<BoosterDTO> quickstart;

    @Override
    public void initializeUI(UIBuilder builder) {
        UIContext context = builder.getUIContext();
        boolean customBoosterCatalog = hasCustomBoosterCatalog(context);
        Collection<RhoarBooster> boosters = catalog.getBoosters(forLegacyOsio().or(b -> customBoosterCatalog));

        Map<String, BoosterDTO> map = new HashMap<>();
        for (RhoarBooster booster : boosters) {
            // TODO lets filter out duplicate named boosters for now
            // as they break the combo box UX
            // once we move away from combo box we can return all versions of all boosters
            // with the same name though!
            String key = booster.getName();
            if (!map.containsKey(key)) {
                map.put(key, new BoosterDTO(booster));
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

    private Predicate<RhoarBooster> forLegacyOsio() {
        return (RhoarBooster b) -> b.getMetadata("worksWithLegacyOsio", false);
    }

    /**
     * Returns true if there is a custom user speific booster catalog
     */
    private boolean hasCustomBoosterCatalog(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();

        return (attributeMap.containsKey(AttributeMapKeys.CATALOG_GIT_REF) && System.getenv(AttributeMapKeys.CATALOG_GIT_REF) == null) ||
                (attributeMap.containsKey(AttributeMapKeys.CATALOG_GIT_REPOSITORY) && System.getenv(AttributeMapKeys.CATALOG_GIT_REPOSITORY) == null);
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
    public NavigationResult next(UINavigationContext context) {
        UIContext uiContext = context.getUIContext();
        updateAttributes(uiContext);
        return null;
    }

    protected void updateAttributes(UIContext uiContext) {
        BoosterDTO boosterDTO = quickstart.getValue();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();
        if (boosterDTO != null) {
            Mission mission = boosterDTO.mission();
            RhoarBooster booster = boosterDTO.booster();
            Runtime runtime = boosterDTO.runtime();

            attributeMap.put(BoosterDTO.class, boosterDTO);
            attributeMap.put(Mission.class, mission);
            attributeMap.put(RhoarBooster.class, booster);
            attributeMap.put(Runtime.class, runtime);
        }
    }

    @Override
    public Result execute(UIExecutionContext context) {
        UIContext uiContext = context.getUIContext();
        updateAttributes(uiContext);
        return Results.success();
    }

}
