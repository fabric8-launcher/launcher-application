package io.fabric8.launcher.core.api.projectiles.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.immutables.value.Value;

import javax.validation.constraints.NotNull;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatorLaunchProjectileContext.class)
@JsonDeserialize(as = ImmutableCreatorLaunchProjectileContext.class)
@JsonIgnoreProperties("clusterId")
public interface CreatorLaunchProjectileContext extends CreatorLaunchingProjectileContext {
    @NotNull ObjectNode getProject();
}
