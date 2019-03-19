package io.fabric8.launcher.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class PathsTest {

    @Test
    void deleteDirectory(@TempDir Path tempDirectory) throws IOException {
        Paths.deleteDirectory(tempDirectory);
        assertThat(tempDirectory).doesNotExist();
    }

    /**
     * See https://snyk.io/research/zip-slip-vulnerability
     */
    @Test
    void unzip_zipslip_vulnerability(@TempDir Path tempDir, @TempDir Path tempDirectory) throws IOException {
        byte[] zip = Paths.zip("../../foo", tempDir);
        Files.delete(tempDirectory);
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> Paths.unzip(new ByteArrayInputStream(zip), tempDirectory));
    }

    /**
     * See https://snyk.io/research/zip-slip-vulnerability
     */
    @Test
    void unzip(@TempDir Path tempDir, @TempDir Path tempDirectory) throws IOException {
        byte[] zip = Paths.zip("foobar", tempDir);
        Files.delete(tempDirectory);
        Paths.unzip(new ByteArrayInputStream(zip), tempDirectory);
        assertThat(tempDirectory).exists();
    }

    /*
     * See https://github.com/fabric8-launcher/launcher-backend/pull/541
     */
    @Test
    void unzip_zip_without_dir_entries(@TempDir Path tempDirectory) throws IOException {
        Files.delete(tempDirectory);
        assertThatCode(() -> {
            try (InputStream is = getClass().getResourceAsStream("pathtest.zip")) {
                Paths.unzip(is, tempDirectory);
                assertThat(tempDirectory).exists();
            }
        }).doesNotThrowAnyException();
    }
}