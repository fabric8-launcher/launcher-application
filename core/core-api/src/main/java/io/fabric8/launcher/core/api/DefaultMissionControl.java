package io.fabric8.launcher.core.api;

import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.context.CreateProjectileContext;

/**
 * The default {@link MissionControl}.
 * Use it when @Injecting it, since it is easier than remembering the correct generics parameters
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface DefaultMissionControl extends MissionControl<CreateProjectileContext, CreateProjectile> {
}
