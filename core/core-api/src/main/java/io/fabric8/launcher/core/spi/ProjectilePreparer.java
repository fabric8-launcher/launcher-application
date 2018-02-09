package io.fabric8.launcher.core.spi;

import java.nio.file.Path;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * Prepares the copied booster before converting into a {@link io.fabric8.launcher.core.api.Projectile}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ProjectilePreparer {

    /**
     * @param projectPath the project {@link Path} where the requested {@link RhoarBooster} was copied to
     * @param booster     the {@link RhoarBooster} used in this request
     * @param context     information from the UI
     */
    void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context);
}
