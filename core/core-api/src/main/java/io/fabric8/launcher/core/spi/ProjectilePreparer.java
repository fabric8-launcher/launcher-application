package io.fabric8.launcher.core.spi;

import java.nio.file.Path;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ProjectilePreparer {

    void prepare(Path path, RhoarBooster booster, ProjectileContext context);
}
