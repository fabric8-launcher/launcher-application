package io.fabric8.launcher.core.api;

import java.nio.file.Path;
import java.util.UUID;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import org.immutables.value.Value;

/**
 * Value object defining the inputs to {@link MissionControl#launch(CreateProjectile)}
 * immutable and pre-checked for valid state during creation.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Projectile {
    /**
     * @return return the unique id for this projectile
     */
    UUID getId();

    /**
     * @return The name to use in creating the new OpenShift project
     */
    String getOpenShiftProjectName();

    Path getProjectLocation();

    Mission getMission();

    Runtime getRuntime();

    String getGitRepositoryName();

    String getGitRepositoryDescription();

    /**
     * @return The start of step
     */
    @Value.Default
    default int getStartOfStep() {
        return 0;
    }
}