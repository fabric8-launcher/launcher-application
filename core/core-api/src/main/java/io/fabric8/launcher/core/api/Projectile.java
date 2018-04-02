package io.fabric8.launcher.core.api;

import java.util.UUID;
import java.util.function.Consumer;

import io.fabric8.launcher.core.api.events.StatusMessageEvent;
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

    /**
     * @return the consumer of events fired from this projectile
     */
    @Value.Default
    default Consumer<StatusMessageEvent> getEventConsumer() {
        // noop impl
        return statusMessageEvent -> {
        };
    }
}