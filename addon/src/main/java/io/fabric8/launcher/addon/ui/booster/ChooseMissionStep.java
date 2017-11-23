/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.booster;

import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import io.fabric8.launcher.addon.BoosterCatalogFactory;
import io.openshift.booster.catalog.DeploymentType;
import io.openshift.booster.catalog.Mission;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
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
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ChooseMissionStep implements UIWizardStep {
    @Inject
    @WithAttributes(label = "Mission", required = true)
    private UISelectOne<Mission> mission;

    @Inject
    private BoosterCatalogFactory catalogServiceFactory;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext context = builder.getUIContext();
        if (context.getProvider().isGUI()) {
            mission.setItemLabelConverter(Mission::getName);
        } else {
            mission.setItemLabelConverter(Mission::getId);
        }
        DeploymentType deploymentType = (DeploymentType) context.getAttributeMap().get(DeploymentType.class);
        String[] filterLabels = catalogServiceFactory.getFilterLabels(context);
        Set<Mission> missions = catalogServiceFactory.getCatalog(context).selector()
                .deploymentType(deploymentType)
                .labels(filterLabels)
                .getMissions();
        mission.setValueChoices(missions);
        mission.setDefaultValue(() -> {
            Iterator<Mission> iterator = mission.getValueChoices().iterator();
            return (iterator.hasNext()) ? iterator.next() : null;
        });
        builder.add(mission);
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        context.getUIContext().getAttributeMap().put(Mission.class, mission.getValue());
        return null;
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Mission")
                .description("Choose the Mission")
                .category(Categories.create("Openshift.io"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return Results.success();
    }

}
