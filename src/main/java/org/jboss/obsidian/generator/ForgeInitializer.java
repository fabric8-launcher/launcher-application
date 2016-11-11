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
package org.jboss.obsidian.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

import org.jboss.obsidian.generator.producer.FurnaceProducer;

/**
 * Initializes Forge add-on repository
 */
public class ForgeInitializer
{
   private static final transient Logger LOG = Logger.getLogger(ForgeInitializer.class.getName());

   /**
    * Called when CDI is initialized
    */
   public void initialize(@Observes @Initialized(ApplicationScoped.class) Object init, FurnaceProducer furnaceProducer)
   {
      System.setProperty("user.home",
               System.getenv().getOrDefault("OPENSHIFT_DATA_DIR", System.getProperty("user.home")));
      // TODO: Move to external configuration
      // lets ensure that the addons folder is initialized
      File repoDir = new File(System.getenv().getOrDefault("OPENSHIFT_DATA_DIR",
               "/home/ggastald/workspace/forge-core/dist/target/forge-distribution-3.3.4-SNAPSHOT/"), "addons");
      LOG.info("initializing furnace with folder: " + repoDir.getAbsolutePath());
      File[] files = repoDir.listFiles();
      if (files == null || files.length == 0)
      {
         LOG.warning("No files found in the addon directory: " + repoDir.getAbsolutePath());
      }
      else
      {
         LOG.warning("Found " + files.length + " addon files in directory: " + repoDir.getAbsolutePath());
      }
      furnaceProducer.setup(repoDir);
   }

   private static Path rootPath;

   public static Path getRoot()
   {
      if (rootPath == null)
      {
         rootPath = Paths.get(System.getenv().getOrDefault("OPENSHIFT_TMP_DIR",
                  "/tmp"), "workspace");
         if (!Files.exists(rootPath))
         {
            try
            {
               Files.createDirectory(rootPath);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
      return rootPath;
   }

   public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init, FurnaceProducer furnaceProducer)
   {
      furnaceProducer.destroy();
   }
}
