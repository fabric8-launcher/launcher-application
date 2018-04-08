package io.fabric8.launcher.osio.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableSpace.class)
@JsonSerialize(as = ImmutableSpace.class)
public interface Space {
    String getId();

    String getName();
}