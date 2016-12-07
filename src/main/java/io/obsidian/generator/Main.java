/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.obsidian.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.management.ManagementFraction;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      Swarm swarm = new Swarm();
      Path keyStorePath = keystorePath();
      // Avoid enabling management port
      swarm.fraction(new ManagementFraction());
      swarm.fraction(UndertowFraction.createDefaultFraction(keyStorePath.toString(), "password", "appserver"));
      swarm.start().deploy();
   }

   private static Path keystorePath() throws IOException
   {
      // Copy keystore to tmp file
      Path tmpFile = Files.createTempFile("keystore", ".jks");
      try (InputStream is = Main.class.getClassLoader().getResourceAsStream("keystore.jks"))
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] b = new byte[4096];
         int i = -1;
         while ((i = is.read(b)) != -1)
         {
            baos.write(b, 0, i);
         }
         Files.write(tmpFile, baos.toByteArray());
      }
      return tmpFile;
   }
}
