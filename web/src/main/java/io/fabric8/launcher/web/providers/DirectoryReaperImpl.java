package io.fabric8.launcher.web.providers;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.core.api.DirectoryReaper;

import static io.fabric8.launcher.base.Paths.deleteDirectory;

/**
 * Deletes temporary directories
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class DirectoryReaperImpl implements DirectoryReaper {

    @Resource
    private ManagedExecutorService executor;

    private static final Logger log = Logger.getLogger(DirectoryReaperImpl.class.getName());


    public void delete(Path path) {
        executor.submit(() -> {
            log.info("Deleting " + path);
            try {
                deleteDirectory(path);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error while deleting" + path, e);
            }
        });
    }
}
