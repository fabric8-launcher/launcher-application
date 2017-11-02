package io.openshift.appdev.missioncontrol.web.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class that helps us with file uploads.
 */
class FileUploadHelper {

    private FileUploadHelper() {
    }

    /**
     * Unzip a zip file into a temporary location
     *
     * @param is the zip file contents to be unzipped
     * @param outputDir the output directory
     * @throws IOException when we could not read the file
     */
    public static void unzip(InputStream is, Path outputDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry zipEntry = null;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path entry = outputDir.resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(entry);
                } else {
                    Files.copy(zis, entry);
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Deletes a directory recursively
     *
     * @param directory
     * @return true if deletion succeeds, false otherwise
     */
    public static boolean deleteDirectory(Path directory) {
        if (directory != null) {
            try {
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
            } catch (IOException ignored) {
                return false;
            }
        }
        return true;
    }

}
