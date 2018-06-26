package io.fabric8.launcher.osio.projectiles;

import javax.annotation.Nullable;

import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.osio.client.Space;
import org.apache.maven.model.Dependency;
import org.immutables.value.Value;

import java.util.List;

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

    @Value.Default
    default boolean isEmptyRepository() {
        return false;
    }

    List<Dependency> projectDependencies();
}
