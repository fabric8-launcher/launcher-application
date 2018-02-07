package io.fabric8.launcher.osio.tenant;

import java.util.Objects;

import io.fabric8.utils.Strings;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface Namespace {
    String getName();

    String getType();

    String getClusterUrl();

    /**
     * Returns true if this namespace is a user namespace
     */
    @Value.Derived
    default boolean isUserNamespace() {
        if (Strings.isNotBlank(getName())) {
            return Objects.equals("user", getType());
        }
        return false;
    }
}
