package io.fabric8.launcher.osio.projectiles;

import javax.annotation.Nullable;

import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.osio.wit.Space;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OsioProjectile extends Projectile {
    String getGitRepositoryName();

    @Nullable
    String getGitOrganization();

    /**
     * @return The name to use in creating the new OpenShift project
     */
    String getOpenShiftProjectName();

    String getPipelineId();

    Space getSpace();
}
