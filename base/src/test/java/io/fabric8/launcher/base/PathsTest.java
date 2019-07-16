package io.fabric8.launcher.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

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
    void unzip_zipslip_vulnerability(@TempDir Path tempDir) throws IOException {
        Files.write(tempDir.resolve("foo.txt"), "test".getBytes());
        byte[] zip = Paths.zip("../../foo", tempDir);
        Paths.deleteDirectory(tempDir);
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> Paths.unzip(new ByteArrayInputStream(zip), tempDir));
    }

    /**
     * See https://snyk.io/research/zip-slip-vulnerability
     */
    @Test
    void unzip(@TempDir Path tempDir) throws IOException {
        Files.write(tempDir.resolve("foo.txt"), "test".getBytes());
        byte[] zip = Paths.zip("foobar", tempDir);
        Paths.deleteDirectory(tempDir);
        Paths.unzip(new ByteArrayInputStream(zip), tempDir);
        assertThat(tempDir).exists();
    }

    /*
     * See https://github.com/fabric8-launcher/launcher-application/pull/541
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

    @Test
    void should_join_paths() {
        assertThat(Paths.join("a", "b")).isEqualTo("a/b");
        assertThat(Paths.join("a", "/b")).isEqualTo("a/b");
        assertThat(Paths.join("a/", "b")).isEqualTo("a/b");
        assertThat(Paths.join("a", "b", "c")).isEqualTo("a/b/c");
    }

    @Test
    void zip_should_preserve_permissions(@TempDir Path tempDir) throws IOException {
        Path fooDir = Files.createDirectory(tempDir.resolve("foo"));
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
        Path file = fooDir.resolve("foo.txt");
        Files.write(file, "test".getBytes());

        Files.setPosixFilePermissions(file, permissions);
        assertThat(file).isExecutable();
        byte[] zip = Paths.zip("foo", fooDir);
        Paths.deleteDirectory(tempDir);
        Paths.unzip(new ByteArrayInputStream(zip), tempDir);
        assertThat(Files.getPosixFilePermissions(file)).hasSameElementsAs(permissions);
    }
}
