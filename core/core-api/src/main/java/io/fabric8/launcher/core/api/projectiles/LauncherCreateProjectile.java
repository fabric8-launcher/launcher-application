package io.fabric8.launcher.core.api.projectiles;

import org.immutables.value.Value;

/**
 * This interface exists because {@link CreateProjectile} cannot use @Value.Immutable and be extended (eg. OsioProjectile)
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface LauncherCreateProjectile extends CreateProjectile {
}
