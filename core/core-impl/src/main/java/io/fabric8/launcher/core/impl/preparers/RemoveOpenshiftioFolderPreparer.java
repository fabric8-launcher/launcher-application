package io.fabric8.launcher.core.impl.preparers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.LauncherProjectileContext;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class RemoveOpenshiftioFolderPreparer implements ProjectilePreparer {

    private Logger logger = Logger.getLogger(RemoveOpenshiftioFolderPreparer.class.getName());

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof LauncherProjectileContext)) {
            // If downloading a zip, delete .openshiftio dir
            Path openshiftIoPath = projectPath.resolve(".openshiftio");
            if (Files.exists(openshiftIoPath)) {
                try {
                    Paths.deleteDirectory(openshiftIoPath);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error while deleting .openshift.io", e);
                }
            }
            // Delete Jenkinsfile if exists
            try {
                Files.deleteIfExists(projectPath.resolve("Jenkinsfile"));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not delete Jenkinsfile", e);
            }
        }
    }
}
