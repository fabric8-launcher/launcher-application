/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.obsidian.generator.producer;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.forge.addon.convert.ConverterFactory;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.command.CommandFactory;
import org.jboss.forge.addon.ui.context.UIContextListener;
import org.jboss.forge.addon.ui.controller.CommandControllerFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;

/**
 * Produces {@link Furnace} service instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FurnaceServiceProducer
{
   private AddonRegistry addonRegistry;

   @Inject
   public FurnaceServiceProducer(Furnace furnace)
   {
      this.addonRegistry = furnace.getAddonRegistry();
   }

   @Produces
   public CommandFactory getCommandFactory()
   {
      return addonRegistry.getServices(CommandFactory.class).get();
   }

   @Produces
   public CommandControllerFactory getControllerFactory()
   {
      return addonRegistry.getServices(CommandControllerFactory.class).get();
   }

   @Produces
   public ResourceFactory getResourceFactory()
   {
      return addonRegistry.getServices(ResourceFactory.class).get();
   }

   @Produces
   public ConverterFactory getConverterFactory()
   {
      return addonRegistry.getServices(ConverterFactory.class).get();
   }

   @Produces
   public Iterable<UIContextListener> getUIContextListeners()
   {
      return addonRegistry.getServices(UIContextListener.class);
   }
}
