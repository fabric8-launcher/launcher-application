package io.fabric8.launcher.base;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.jupiter.api.Test;

import static io.fabric8.launcher.base.PosixFilePermissionSupport.toOctalFileMode;
import static io.fabric8.launcher.base.PosixFilePermissionSupport.toPosixFilePermissions;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.assertj.core.api.Assertions.assertThat;

class PosixFilePermissionSupportTest {

    @Test
    void should_compute_octal_value() {
        assertThat(toOctalFileMode(fromString("---------"))).isEqualTo(0b000_000_000);
        assertThat(toOctalFileMode(fromString("--------x"))).isEqualTo(0b000_000_001);
        assertThat(toOctalFileMode(fromString("-------w-"))).isEqualTo(0b000_000_010);
        assertThat(toOctalFileMode(fromString("-------wx"))).isEqualTo(0b000_000_011);
        assertThat(toOctalFileMode(fromString("------rw-"))).isEqualTo(0b000_000_110);
        assertThat(toOctalFileMode(fromString("------rwx"))).isEqualTo(0b000_000_111);

        assertThat(toOctalFileMode(fromString("-----x---"))).isEqualTo(0b000_001_000);
        assertThat(toOctalFileMode(fromString("-----x--x"))).isEqualTo(0b000_001_001);
    }


    @Test
    void should_compute_permission_from_octal() {
        assertThat(toPosixFilePermissions(0b000_000_000)).isEmpty();
        assertThat(toPosixFilePermissions(0b000_000_001))
                .containsExactly(PosixFilePermission.OTHERS_EXECUTE);
        assertThat(toPosixFilePermissions(0b000_000_111))
                .containsExactly(PosixFilePermission.OTHERS_READ,
                                 PosixFilePermission.OTHERS_WRITE,
                                 PosixFilePermission.OTHERS_EXECUTE);
        assertThat(toPosixFilePermissions(0b000_000_111))
                .containsExactly(PosixFilePermission.OTHERS_READ,
                                 PosixFilePermission.OTHERS_WRITE,
                                 PosixFilePermission.OTHERS_EXECUTE);

    }
}