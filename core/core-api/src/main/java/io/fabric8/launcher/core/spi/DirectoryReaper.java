package io.fabric8.launcher.core.spi;

import java.nio.file.Path;

/**
 * Deletes temporary directories
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface DirectoryReaper {

    void delete(Path path);
}
