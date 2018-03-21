package io.fabric8.launcher.osio.wit;

import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface Space {

    String getId();

    String getName();

    @Value.Derived
    default String getPath() {
        return "/" + getName();
    }
}