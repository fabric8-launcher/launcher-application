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

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.proxy.ClassLoaderAdapterBuilder;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.util.AddonCompatibilityStrategies;
import org.jboss.forge.furnace.util.Sets;

/**
 * Produces {@link Furnace} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class FurnaceProducer
{
   private Furnace furnace;

   public void setup(File repoDir)
   {
      // Initialize Furnace
      ClassLoader ccl = Thread.currentThread().getContextClassLoader();
      furnace = create(ccl, ccl);
      furnace.setAddonCompatibilityStrategy(AddonCompatibilityStrategies.LENIENT);
      furnace.addRepository(AddonRepositoryMode.IMMUTABLE, repoDir);
      Future<Furnace> future = furnace.startAsync();
      try
      {
         future.get();
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new RuntimeException("Furnace failed to start.", e);
      }
   }

   /**
    * Produce a {@link Furnace} instance using the first given {@link ClassLoader} to act as the client for which
    * {@link Class} instances should be translated across {@link ClassLoader} boundaries, and the second given
    * {@link ClassLoader} to load core furnace implementation classes.
    */
   private Furnace create(final ClassLoader clientLoader, final ClassLoader furnaceLoader)
   {
      try
      {
         Class<?> furnaceType = furnaceLoader.loadClass("org.jboss.forge.furnace.impl.FurnaceImpl");
         final Object instance = furnaceType.newInstance();

         final Furnace furnace = (Furnace) ClassLoaderAdapterBuilder
                  .callingLoader(clientLoader)
                  .delegateLoader(furnaceLoader)
                  .enhance(instance, Furnace.class);

         Callable<Set<ClassLoader>> whitelistCallback = new Callable<Set<ClassLoader>>()
         {
            volatile long lastRegistryVersion = -1;
            final Set<ClassLoader> result = Sets.getConcurrentSet();

            @Override
            public Set<ClassLoader> call() throws Exception
            {
               if (furnace.getStatus().isStarted())
               {
                  long registryVersion = furnace.getAddonRegistry().getVersion();
                  if (registryVersion != lastRegistryVersion)
                  {
                     result.clear();
                     lastRegistryVersion = registryVersion;
                     for (Addon addon : furnace.getAddonRegistry().getAddons())
                     {
                        ClassLoader classLoader = addon.getClassLoader();
                        if (classLoader != null)
                           result.add(classLoader);
                     }
                  }
               }

               return result;
            }
         };

         return (Furnace) ClassLoaderAdapterBuilder
                  .callingLoader(clientLoader)
                  .delegateLoader(furnaceLoader)
                  .whitelist(whitelistCallback)
                  .enhance(instance, Furnace.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Produces
   public Furnace getFurnace()
   {
      return furnace;
   }

   @PreDestroy
   public void destroy()
   {
      furnace.stop();
   }
}
