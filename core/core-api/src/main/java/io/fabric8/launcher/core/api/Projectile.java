package io.fabric8.launcher.core.api;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Value object defining the inputs to {@link MissionControl#launch(Projectile)}
 * immutable and pre-checked for valid state during creation.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Projectile {

    /**
     * @return return the unique id for this projectile
     */
    UUID getId();

    Path getProjectLocation();

    /**
     * @return The start of step
     */
    int getStartOfStep();

//    //TODO: Move the methods below to OSIO
//    String getTargetEnvironment();
//
//    String getPipelineId();
//
//    String getSpacePath();
}