package io.fabric8.launcher.osio.projectiles;

import io.fabric8.launcher.core.api.CreateProjectile;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface OsioProjectile extends CreateProjectile {
    String getPipelineId();

    String getSpacePath();
}
