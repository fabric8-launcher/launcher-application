package io.fabric8.launcher.osio.projectiles;

import java.util.UUID;

import io.fabric8.launcher.core.api.Projectile;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface ImportProjectile extends Projectile {

    String getGitOrganization();

    String getGitRepository();
}
