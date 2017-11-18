/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.booster;

import java.util.Iterator;
import java.util.Optional;

import javax.inject.Inject;

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

import io.fabric8.launcher.addon.BoosterCatalogFactory;
import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.DeploymentType;
import io.openshift.booster.catalog.Mission;
import io.openshift.booster.catalog.Runtime;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ChooseRuntimeStep implements UIWizardStep
{
   @Inject
   private BoosterCatalogFactory catalogServiceFactory;

   @Inject
   @WithAttributes(label = "Runtime", required = true)
   private UISelectOne<Runtime> runtime;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      UIContext context = builder.getUIContext();
      if (context.getProvider().isGUI())
      {
         runtime.setItemLabelConverter(Runtime::getName);
      }
      else
      {
         runtime.setItemLabelConverter(Runtime::getId);
      }

      runtime.setValueChoices(() -> {
         DeploymentType deploymentType = (DeploymentType) context.getAttributeMap().get(DeploymentType.class);
         Mission mission = (Mission) context.getAttributeMap().get(Mission.class);
         String[] filterLabels = catalogServiceFactory.getFilterLabels(builder.getUIContext());
         return catalogServiceFactory.getCatalog(context).selector()
                 .deploymentType(deploymentType)
                 .mission(mission)
                 .labels(filterLabels)
                 .getRuntimes();
      });

      runtime.setDefaultValue(() -> {
         Iterator<Runtime> iterator = runtime.getValueChoices().iterator();
         return (iterator.hasNext()) ? iterator.next() : null;
      });

      builder.add(runtime);
   }

   @Override
   public void validate(UIValidationContext context)
   {
      UIContext uiContext = context.getUIContext();
      Mission mission = (Mission) uiContext.getAttributeMap().get(Mission.class);
      String[] filterLabels = catalogServiceFactory.getFilterLabels(uiContext);

      Optional<Booster> booster = catalogServiceFactory.getCatalog(uiContext).getBooster(mission,
               runtime.getValue(), filterLabels);
      if (!booster.isPresent())
      {
         context.addValidationError(runtime,
                  "No booster found for mission '" + mission + "' and runtime '" + runtime.getValue() + "'");
      }
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(getClass()).name("Runtime")
               .description("Choose the runtime for your mission")
               .category(Categories.create("Openshift.io"));
   }

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception
   {
      context.getUIContext().getAttributeMap().put(Runtime.class, runtime.getValue());
      return null;
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results.success();
   }

}
