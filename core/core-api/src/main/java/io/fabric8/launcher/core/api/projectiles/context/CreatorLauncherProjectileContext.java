package io.fabric8.launcher.core.api.projectiles.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.core.api.ProjectileContext;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatorLauncherProjectileContext.class)
@JsonDeserialize(as = ImmutableCreatorLauncherProjectileContext.class)
@JsonIgnoreProperties("clusterId")
public interface CreatorLauncherProjectileContext extends ProjectileContext, ProjectNameCapable, GitCapable {
    ObjectNode getProject();
}