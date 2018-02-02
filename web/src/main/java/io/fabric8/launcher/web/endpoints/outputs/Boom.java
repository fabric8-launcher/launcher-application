package io.fabric8.launcher.web.endpoints.outputs;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBoom.class)
public interface Boom {

    @Value.Parameter
    @JsonProperty("uuid")
    UUID getUUID();

    @Value.Derived
    @JsonProperty("uuid_link")
    default String getUUIDLink() {
        return "/status/" + getUUID();
    }
}
