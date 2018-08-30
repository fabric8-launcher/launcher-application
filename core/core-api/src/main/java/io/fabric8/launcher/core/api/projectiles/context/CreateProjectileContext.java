package io.fabric8.launcher.core.api.projectiles.context;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface CreateProjectileContext extends ProjectileContext {

    Mission getMission();

    Runtime getRuntime();

    Version getRuntimeVersion();

    String getGroupId();

    String getArtifactId();

    String getProjectVersion();

    String getProjectName();

}
