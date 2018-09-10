package io.fabric8.launcher.core.api.projectiles.context;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface BoosterCapable {
    Mission getMission();

    Runtime getRuntime();

    Version getRuntimeVersion();

}
