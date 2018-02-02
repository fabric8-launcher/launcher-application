package io.fabric8.launcher.core.api;

import java.util.UUID;

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

    /**
     * @return The start of step
     */
    int getStartOfStep();
}