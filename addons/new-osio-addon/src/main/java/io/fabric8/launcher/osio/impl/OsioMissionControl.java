package io.fabric8.launcher.osio.impl;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(OSIO)
public class OsioMissionControl implements MissionControl {

    @Inject
    @Default
    private MissionControl missionControl;


    @Override
    public Projectile prepare(ProjectileContext context) {
        return missionControl.prepare(context);
    }

    @Override
    public Boom launch(Projectile projectile) throws IllegalArgumentException {
        return missionControl.launch(projectile);
    }
}
