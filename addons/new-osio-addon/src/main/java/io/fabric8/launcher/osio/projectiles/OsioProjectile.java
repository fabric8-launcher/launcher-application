package io.fabric8.launcher.osio.projectiles;

import io.fabric8.launcher.core.api.Projectile;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OsioProjectile extends Projectile {
    String getGitRepositoryName();

    String getGitOrganization();

    /**
     * @return The name to use in creating the new OpenShift project
     */
    String getOpenShiftProjectName();

    String getPipelineId();

    String getSpacePath();
}
