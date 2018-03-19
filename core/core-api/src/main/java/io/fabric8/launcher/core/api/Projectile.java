package io.fabric8.launcher.core.api;

import java.util.UUID;

import org.immutables.value.Value;

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
    @Value.Default
    default UUID getId() {
        return UUID.randomUUID();
    }

    /**
     * @return The start of step
     */
    @Value.Default
    default int getStartOfStep() {
        return 0;
    }
}