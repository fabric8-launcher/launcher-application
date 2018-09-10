package io.fabric8.launcher.core.api.projectiles.context;

import io.fabric8.launcher.core.api.Projectile;

/**
 * The context necessary to create a {@link Projectile} through Fabric8 Launcher (developers.redhat.com/launch)
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface LauncherProjectileContext extends CreateProjectileContext, GitCapable {

}