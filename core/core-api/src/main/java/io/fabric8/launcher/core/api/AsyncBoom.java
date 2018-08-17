package io.fabric8.launcher.core.api;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.launcher.core.api.events.StatusEventKind;
import org.immutables.value.Value;

/**
 * This is the JSON representation returned to the caller when /launch is called.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAsyncBoom.class)
@JsonDeserialize(as = ImmutableAsyncBoom.class)
public interface AsyncBoom {
    /**
     * @return the projectile UUID
     */
    UUID uuid();

    @Value.Default
    @JsonProperty("uuid_link")
    default String uuidLink() {
        return "/status/" + uuid();
    }

    List<Event> events();


    @Value.Immutable
    @JsonSerialize(as = ImmutableEvent.class)
    @JsonDeserialize(as = ImmutableEvent.class)
    interface Event {
        @Value.Parameter
        String name();

        @Value.Parameter
        String message();

        static Event from(StatusEventKind eventType) {
            return ImmutableEvent.of(eventType.name(), eventType.message());
        }
    }

    interface AsyncBoomBuilder {
        default AsyncBoomBuilder eventTypes(Collection<StatusEventKind> eventTypes) {
            eventTypes.stream().map(Event::from).forEach(this::addEvents);
            return this;
        }

        AsyncBoomBuilder addEvents(Event event);

        AsyncBoom build();
    }

    abstract class Builder implements AsyncBoomBuilder {

    }

}