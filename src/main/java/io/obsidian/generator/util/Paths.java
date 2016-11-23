/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.obsidian.generator.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Paths
{

   public static byte[] zip(String root, final java.nio.file.Path directory) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (final ZipOutputStream zos = new ZipOutputStream(baos))
      {
         Files.walkFileTree(directory, new SimpleFileVisitor<java.nio.file.Path>()
         {
            @Override
            public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException
            {
               String entry = root + "/" + directory.relativize(file).toString();
               zos.putNextEntry(new ZipEntry(entry));
               Files.copy(file, zos);
               zos.closeEntry();
               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
               String entry = root + "/" + directory.relativize(dir).toString() + "/";
               zos.putNextEntry(new ZipEntry(entry));
               zos.closeEntry();
               return FileVisitResult.CONTINUE;
            }
         });
      }
      return baos.toByteArray();
   }

   public static void deleteDirectory(java.nio.file.Path directory) throws IOException
   {
      Files.walkFileTree(directory, new SimpleFileVisitor<java.nio.file.Path>()
      {
         @Override
         public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException
         {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException
         {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
         }
      });
   }
}
