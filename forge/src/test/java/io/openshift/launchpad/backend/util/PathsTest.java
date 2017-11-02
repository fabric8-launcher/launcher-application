package io.openshift.launchpad.backend.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test for Paths
 */
public class PathsTest {

    @Test
    public void shouldZipFolder() throws Exception {
        //given
        Path folder = Files.createTempDirectory("junit");
        Path tempFile = Files.createTempFile(folder, "test", "file");
        String content = "content";
        Files.write(tempFile, content.getBytes());
        String rootFolderName = "rootFolder";

        //when
        byte[] result = Paths.zip(rootFolderName, folder);

        //then
        Path proof = Files.createTempFile("proof", "zip");
        Files.write(proof, result);
        File tempDirectory = Files.createTempDirectory("proof").toFile();
        unzip(proof.toFile(), tempDirectory);

        assertArrayEquals(tempDirectory.list(), new String[] {rootFolderName});
        File rootFolder = new File(tempDirectory, rootFolderName);
        String contentFileName = tempFile.getFileName().toString();
        assertArrayEquals(rootFolder.list(), new String[] {contentFileName});
        Path contentFile = java.nio.file.Paths.get(rootFolder.toURI()).resolve(contentFileName);
        assertEquals(Files.readAllLines(contentFile), Collections.singletonList(content));
    }

    @Test
    public void deleteDirectory() throws Exception {
        //given
        Path given = Files.createTempDirectory("given");
        Path resolve = given.resolve("sub").resolve("sub1");
        resolve.toFile().mkdirs();

        //when
        Paths.deleteDirectory(given);

        //then
        assertFalse(given.toFile().exists());
    }

    private static void unzip(File zipFile, File outputFolder) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder, fileName);
                newFile.getParentFile().mkdirs();
                if (!ze.isDirectory()) {
                    Files.copy(zis, newFile.toPath());
                }
                ze = zis.getNextEntry();
            }
        }
    }

}