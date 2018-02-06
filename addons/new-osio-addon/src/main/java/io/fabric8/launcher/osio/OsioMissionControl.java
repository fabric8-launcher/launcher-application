package io.fabric8.launcher.osio;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.importing.ImportProjectile;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(OSIO)
@RequestScoped
public class OsioMissionControl implements MissionControl {

    @Inject
    @Application(LAUNCHER)
    private MissionControl missionControl;

    @Override
    public void validate(ProjectileContext context) throws ConstraintViolationException {

    }

    @Override
    public Projectile prepare(ProjectileContext context) {
        return missionControl.prepare(context);
    }

    @Override
    public Boom launch(Projectile projectile) throws IllegalArgumentException {
        return missionControl.launch(projectile);
    }


    public Boom importRepository(ImportProjectile importProjectile) {
        return null;
    }
}
