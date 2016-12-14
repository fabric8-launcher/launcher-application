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
package org.obsidiantoaster.generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.forge.service.producer.FurnaceProducer;

/**
 * Initializes Forge add-on repository
 */
@WebListener
public class ForgeInitializer implements ServletContextListener
{
   private static final transient Logger LOG = Logger.getLogger(ForgeInitializer.class.getName());

   @Inject
   FurnaceProducer furnaceProducer;

   @Override
   public void contextInitialized(ServletContextEvent sce)
   {
      try
      {
         File repoDir = new File(sce.getServletContext().getResource("/WEB-INF/addons").toURI());
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
      catch (URISyntaxException | MalformedURLException e)
      {
         LOG.log(Level.SEVERE, "Error while setting up Furnace", e);
      }
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce)
   {
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
}
