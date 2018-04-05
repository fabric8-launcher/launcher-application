package io.fabric8.launcher.core.spi;

import java.nio.file.Path;

import javax.annotation.Nullable;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * Prepares the copied booster before converting into a {@link Projectile}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ProjectilePreparer {

    /**
     * @param projectPath the project {@link Path} where the requested {@link RhoarBooster} was copied to
     * @param booster     the {@link RhoarBooster} used in this request. May be null in some cases
     * @param context     information from the UI
     */
    void prepare(Path projectPath, @Nullable RhoarBooster booster, ProjectileContext context);
}
