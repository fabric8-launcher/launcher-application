package io.fabric8.launcher.core.impl.preparers;

import java.nio.file.Path;

import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(LAUNCHER)
public class ChangeMavenPreparer implements ProjectilePreparer {

    @Override
    public void prepare(Path path, ProjectileContext context) {

    }
}
