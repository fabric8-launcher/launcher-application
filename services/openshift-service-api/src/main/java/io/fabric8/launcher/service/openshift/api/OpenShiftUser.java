package io.fabric8.launcher.service.openshift.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOpenShiftUser.class)
@JsonDeserialize(as = ImmutableOpenShiftUser.class)
public interface OpenShiftUser {

    @Value.Parameter
    String getName();
}
