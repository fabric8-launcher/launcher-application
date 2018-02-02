package io.fabric8.launcher.core.api;

import org.immutables.value.Value;

/**
 * Value object defining the inputs to {@link MissionControl#launch(CreateProjectile)}
 * immutable and pre-checked for valid state during creation.
 *
 * This projectile is used to create a project in the users github.
 */
@Value.Immutable
public interface CreateProjectile extends Projectile {

}
