package io.fabric8.launcher.osio.tenant;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableNamespace.class)
@JsonSerialize(as = ImmutableNamespace.class)
public interface Namespace {

    String getName();

    String getType();

    String getClusterUrl();

    String getClusterConsoleUrl();

    /**
     * Returns true if this namespace is a user namespace
     */
    @Value.Derived
    @JsonIgnore
    default boolean isUserNamespace() {
        return Objects.equals("user", getType());
    }
}
