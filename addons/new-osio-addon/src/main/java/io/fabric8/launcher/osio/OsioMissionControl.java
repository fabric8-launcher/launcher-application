package io.fabric8.launcher.osio;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioProjectile;
import io.fabric8.launcher.osio.projectiles.ImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectileContext;
import io.fabric8.launcher.osio.steps.GitSteps;
import io.fabric8.launcher.osio.steps.OpenShiftSteps;
import io.fabric8.launcher.service.git.api.GitRepository;

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

    @Inject
    private GitSteps gitSteps;

    @Inject
    private OpenShiftSteps openShiftSteps;

    @Override
    public void validate(ProjectileContext context) throws ConstraintViolationException {

    }

    @Override
    public Projectile prepare(ProjectileContext genericContext) {
        if (!(genericContext instanceof OsioProjectileContext)) {
            throw new IllegalArgumentException("OsioMissionControl only supports " + OsioProjectileContext.class.getName() + " instances");
        }
        OsioProjectileContext context = (OsioProjectileContext) genericContext;
        Projectile projectile = missionControl.prepare(context);
        return ImmutableOsioProjectile.builder()
                .from(projectile)
                .spacePath(context.getSpacePath())
                .pipelineId(context.getPipelineId())
                .build();
    }

    @Override
    public Boom launch(Projectile genericProjectile) throws IllegalArgumentException {
        if (!(genericProjectile instanceof OsioProjectile)) {
            throw new IllegalArgumentException("OsioMissionControl only supports " + OsioProjectile.class.getName() + " instances");
        }
        OsioProjectile projectile = (OsioProjectile) genericProjectile;
        GitRepository repository = gitSteps.createRepository(projectile);

        openShiftSteps.createBuildConfig(projectile, repository);
        openShiftSteps.createJenkinsConfigMap(repository);

        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);
        gitSteps.pushToGitRepository(projectile, repository);

        return null;
//        2. Code is pushed to the repository
//        5. Build is triggered (if the webhook is created, pushing the code to the repository will automatically trigger the build)
        gitSteps.pushToGitRepository(projectile, repository);
        return ImmutableBoom.builder()
                .createdRepository(repository)
                .createdProject(null)
                .build();
    }


    public Boom launch(ImportProjectile importProjectile) {
        return null;
    }
}
