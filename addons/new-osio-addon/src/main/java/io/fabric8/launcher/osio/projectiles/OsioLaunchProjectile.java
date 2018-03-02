package io.fabric8.launcher.osio.projectiles;

import io.fabric8.launcher.core.api.CreateProjectile;
import org.immutables.value.Value;

@Value.Immutable
public interface OsioLaunchProjectile extends OsioProjectile, CreateProjectile {
}
