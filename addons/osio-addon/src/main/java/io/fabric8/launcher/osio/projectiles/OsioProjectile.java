package io.fabric8.launcher.osio.projectiles;

import javax.annotation.Nullable;

import io.fabric8.launcher.core.api.Projectile;
import org.immutables.value.Value;

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

    String getSpacePath();

    /**
     * @return space path minus the initial slash
     */
    @Value.Derived
    default String getSpaceName() {
        return getSpacePath().substring(1);
    }
}
