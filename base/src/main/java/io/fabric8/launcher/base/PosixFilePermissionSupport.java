package io.fabric8.launcher.base;

import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Converts {@link PosixFilePermission} to octal and vice-versa
 */
class PosixFilePermissionSupport {

    private static final int OWNER_READ_FILEMODE =  0b100_000_000;
    private static final int OWNER_WRITE_FILEMODE = 0b010_000_000;
    private static final int OWNER_EXEC_FILEMODE =  0b001_000_000;

    private static final int GROUP_READ_FILEMODE =  0b000_100_000;
    private static final int GROUP_WRITE_FILEMODE = 0b000_010_000;
    private static final int GROUP_EXEC_FILEMODE =  0b000_001_000;

    private static final int OTHERS_READ_FILEMODE =  0b000_000_100;
    private static final int OTHERS_WRITE_FILEMODE = 0b000_000_010;
    private static final int OTHERS_EXEC_FILEMODE =  0b000_000_001;

    private PosixFilePermissionSupport() {
    }

    /**
     * Converts a set of {@link PosixFilePermission} to chmod-style octal file mode.
     */
    static int toOctalFileMode(Set<PosixFilePermission> permissions) {
        int result = 0;
        for (PosixFilePermission permissionBit : permissions) {
            switch (permissionBit) {
                case OWNER_READ:
                    result |= OWNER_READ_FILEMODE;
                    break;
                case OWNER_WRITE:
                    result |= OWNER_WRITE_FILEMODE;
                    break;
                case OWNER_EXECUTE:
                    result |= OWNER_EXEC_FILEMODE;
                    break;
                case GROUP_READ:
                    result |= GROUP_READ_FILEMODE;
                    break;
                case GROUP_WRITE:
                    result |= GROUP_WRITE_FILEMODE;
                    break;
                case GROUP_EXECUTE:
                    result |= GROUP_EXEC_FILEMODE;
                    break;
                case OTHERS_READ:
                    result |= OTHERS_READ_FILEMODE;
                    break;
                case OTHERS_WRITE:
                    result |= OTHERS_WRITE_FILEMODE;
                    break;
                case OTHERS_EXECUTE:
                    result |= OTHERS_EXEC_FILEMODE;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    static Set<PosixFilePermission> toPosixFilePermissions(int octalFileMode) {
        Set<PosixFilePermission> permissions = new LinkedHashSet<>();
        // Owner
        if ((octalFileMode & OWNER_READ_FILEMODE) == OWNER_READ_FILEMODE) {
            permissions.add(PosixFilePermission.OWNER_READ);
        }
        if ((octalFileMode & OWNER_WRITE_FILEMODE) == OWNER_WRITE_FILEMODE) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((octalFileMode & OWNER_EXEC_FILEMODE) == OWNER_EXEC_FILEMODE) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        // Group
        if ((octalFileMode & GROUP_READ_FILEMODE) == GROUP_READ_FILEMODE) {
            permissions.add(PosixFilePermission.GROUP_READ);
        }
        if ((octalFileMode & GROUP_WRITE_FILEMODE) == GROUP_WRITE_FILEMODE) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((octalFileMode & GROUP_EXEC_FILEMODE) == GROUP_EXEC_FILEMODE) {
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        }
        // Others
        if ((octalFileMode & OTHERS_READ_FILEMODE) == OTHERS_READ_FILEMODE) {
            permissions.add(PosixFilePermission.OTHERS_READ);
        }
        if ((octalFileMode & OTHERS_WRITE_FILEMODE) == OTHERS_WRITE_FILEMODE) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((octalFileMode & OTHERS_EXEC_FILEMODE) == OTHERS_EXEC_FILEMODE) {
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return permissions;
    }
}