package io.fabric8.launcher.core.api.projectiles.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreatorImportLaunchProjectileContext.class)
@JsonDeserialize(as = ImmutableCreatorImportLaunchProjectileContext.class)
@JsonIgnoreProperties("clusterId")
public interface CreatorImportLaunchProjectileContext extends CreatorLaunchingProjectileContext, ImportCapable {
}
