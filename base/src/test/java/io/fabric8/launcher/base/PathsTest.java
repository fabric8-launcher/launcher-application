package io.fabric8.launcher.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class PathsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void deleteDirectory() throws IOException {
        Path tempDirectory = temporaryFolder.newFolder().toPath();
        Paths.deleteDirectory(tempDirectory);
        assertThat(tempDirectory).doesNotExist();
    }

    /**
     * See https://snyk.io/research/zip-slip-vulnerability
     */
    @Test
    public void unzip_zipslip_vulnerability() throws IOException {
        Path tempDir = temporaryFolder.newFolder().toPath();
        byte[] zip = Paths.zip("../../foo", tempDir);
        Path tempDirectory = temporaryFolder.newFolder().toPath();
        Files.delete(tempDirectory);
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> Paths.unzip(new ByteArrayInputStream(zip), tempDirectory));
    }

    /**
     * See https://snyk.io/research/zip-slip-vulnerability
     */
    @Test
    public void unzip() throws IOException {
        Path tempDir = temporaryFolder.newFolder().toPath();
        byte[] zip = Paths.zip("foobar", tempDir);
        Path tempDirectory = temporaryFolder.newFolder().toPath();
        Files.delete(tempDirectory);
        Paths.unzip(new ByteArrayInputStream(zip), tempDirectory);
        assertThat(tempDirectory).exists();
    }

    /*
     * See https://github.com/fabric8-launcher/launcher-backend/pull/541
     */
    @Test
    public void unzip_zip_without_dir_entries() throws IOException {
        Path tempDirectory = temporaryFolder.newFolder().toPath();
        Files.delete(tempDirectory);
        assertThatCode(() -> {
            try (InputStream is = getClass().getResourceAsStream("pathtest.zip")) {
                Paths.unzip(is, tempDirectory);
                assertThat(tempDirectory).exists();
            }
        }).doesNotThrowAnyException();
    }

}