package io.fabric8.launcher.service.openshift.api;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableOpenShiftCluster.class)
@JsonSerialize(as = ImmutableOpenShiftCluster.class)
@JsonIgnoreProperties(value = "apiUrl", allowSetters = true)
public interface OpenShiftCluster {

    String getId();

    @Value.Default
    default String getName() {
        return getId();
    }

    @Nullable
    String getType();

    String getApiUrl();

    @Nullable
    String getConsoleUrl();
}
