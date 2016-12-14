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
