package io.fabric8.launcher.core.api.projectiles.context;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.core.api.ProjectileContext;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatorZipProjectileContext.class)
@JsonDeserialize(as = ImmutableCreatorZipProjectileContext.class)
public interface CreatorZipProjectileContext extends ProjectileContext {
    ObjectNode getProject();
}