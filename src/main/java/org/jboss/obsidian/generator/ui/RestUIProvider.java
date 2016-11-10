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
package org.jboss.obsidian.generator.ui;

import java.io.ByteArrayOutputStream;

import org.jboss.forge.addon.ui.DefaultUIDesktop;
import org.jboss.forge.addon.ui.UIDesktop;
import org.jboss.forge.addon.ui.UIProvider;
import org.jboss.forge.addon.ui.output.UIOutput;

public class RestUIProvider implements UIProvider
{
   private ByteArrayOutputStream out = new ByteArrayOutputStream();
   private ByteArrayOutputStream err = new ByteArrayOutputStream();
   private final UIOutput output = new RestUIOutput(out, err);
   private final UIDesktop desktop = new DefaultUIDesktop();
   private boolean gui = true;

   @Override
   public boolean isGUI()
   {
      return gui;
   }

   /**
    * @param gui the gui to set
    */
   public void setGUI(boolean gui)
   {
      this.gui = gui;
   }

   @Override
   public UIOutput getOutput()
   {
      return output;
   }

   @Override
   public UIDesktop getDesktop()
   {
      return desktop;
   }

   public String getOut()
   {
      return out.toString();
   }

   public String getErr()
   {
      return err.toString();
   }

   @Override
   public String getName()
   {
      return "REST UIProvider";
   }

   @Override
   public boolean isEmbedded()
   {
      return true;
   }

   public void clearOutput()
   {
      out.reset();
      err.reset();
   }

}
