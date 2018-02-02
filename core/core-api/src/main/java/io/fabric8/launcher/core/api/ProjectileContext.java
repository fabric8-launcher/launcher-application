package io.fabric8.launcher.core.api;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;

/**
 * The minimim information to generate a booster
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ProjectileContext {

    Mission getMission();

    Runtime getRuntime();

    Version getRuntimeVersion();

    String getProjectName();

    String getGroupId();

    String getArtifactId();

    String getProjectVersion();
}
