/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.booster;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

import io.fabric8.launcher.addon.BoosterCatalogFactory;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
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

import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRunsOn;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @deprecated
 */
public class ChooseMissionStep implements UIWizardStep {
    @Inject
    @WithAttributes(label = "Mission", required = true)
    private UISelectOne<Mission> mission;

    @Inject
    private BoosterCatalogFactory catalogServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext context = builder.getUIContext();
        if (context.getProvider().isGUI()) {
            mission.setItemLabelConverter(Mission::getName);
        } else {
            mission.setItemLabelConverter(Mission::getId);
        }
        DeploymentType deploymentType = (DeploymentType) context.getAttributeMap().get(DeploymentType.class);
        Predicate<RhoarBooster> filter = x -> true;
        if (deploymentType == DeploymentType.CD) {
            String openShiftCluster = (String) context.getAttributeMap().get("OPENSHIFT_CLUSTER");
            Optional<OpenShiftCluster> cluster = clusterRegistry.findClusterById(openShiftCluster);
            filter = cluster.filter(x -> x.getType() != null)
                    .map(c -> withRunsOn(c.getType()))
                    .orElse(filter);
        }
        Set<Mission> missions = catalogServiceFactory.getCatalog(context).getMissions(filter);
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
                .category(Categories.create("Fabric8"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return Results.success();
    }

}
