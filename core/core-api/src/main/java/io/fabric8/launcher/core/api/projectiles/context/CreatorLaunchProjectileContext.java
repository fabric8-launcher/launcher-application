package io.fabric8.launcher.core.api.projectiles.context;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatorLaunchProjectileContext.class)
@JsonDeserialize(as = ImmutableCreatorLaunchProjectileContext.class)
@JsonIgnoreProperties({"clusterId", "clusterType"})
public interface CreatorLaunchProjectileContext extends CreatorLaunchingProjectileContext {
    @NotNull ObjectNode getProject();
}
