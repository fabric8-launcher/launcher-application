/**
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.launcher.web.forge.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * {@link Path} related operations
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Paths {

    /**
     * Zips an entire directory and returns as a byte[]
     *
     * @param root the root directory to be used
     * @param directory the directory to be zipped
     * @return a byte[] representing the zipped directory
     * @throws IOException if any I/O error happens
     */
    public static byte[] zip(String root, final Path directory) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        zip(root, directory, baos);
        return baos.toByteArray();
    }

    /**
     * Zips an entire directory and stores in the provided {@link OutputStream}
     *
     * @param root the root directory to be used
     * @param directory the directory to be zipped
     * @param os the {@link OutputStream} which the zip operation will be written to
     * @throws IOException if any I/O error happens
     */
    public static void zip(String root, final Path directory, OutputStream os) throws IOException {
        try (final ZipOutputStream zos = new ZipOutputStream(os)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entry = root + File.separator + directory.relativize(file).toString();
                    zos.putNextEntry(new ZipEntry(entry));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String entry = root + File.separator + directory.relativize(dir).toString() + File.separator;
                    zos.putNextEntry(new ZipEntry(entry));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Deletes a directory recursively
     *
     * @param directory
     * @throws IOException
     */
    public static void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
