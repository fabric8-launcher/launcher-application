package io.fabric8.launcher.osio;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectileContext;
import io.fabric8.launcher.osio.steps.GitSteps;
import io.fabric8.launcher.osio.steps.OpenShiftSteps;
import io.fabric8.launcher.osio.steps.WitSteps;
import io.fabric8.launcher.osio.wit.SpaceRegistry;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftProject;
import io.fabric8.openshift.api.model.BuildConfig;

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

    @Inject
    private WitSteps witSteps;

    @Inject
    private SpaceRegistry spaceRegistry;

    @Override
    public OsioLaunchProjectile prepare(ProjectileContext genericContext) {
        if (!(genericContext instanceof OsioProjectileContext)) {
            throw new IllegalArgumentException("OsioMissionControl only supports " + OsioProjectileContext.class.getName() + " instances");
        }
        OsioProjectileContext context = (OsioProjectileContext) genericContext;
        Projectile projectile = missionControl.prepare(context);
        return ImmutableOsioLaunchProjectile.builder()
                .from(projectile)
                .gitOrganization(context.getGitOrganization())
                .space(spaceRegistry.findSpaceByID(context.getSpaceId()))
                .pipelineId(context.getPipelineId())
                .build();
    }

    @Override
    public Boom launch(Projectile genericProjectile) throws IllegalArgumentException {
        if (!(genericProjectile instanceof OsioLaunchProjectile)) {
            throw new IllegalArgumentException("OsioMissionControl only supports " + OsioLaunchProjectile.class.getName() + " instances");
        }
        OsioLaunchProjectile projectile = (OsioLaunchProjectile) genericProjectile;
        GitRepository repository = gitSteps.createRepository(projectile);

        BuildConfig buildConfig = openShiftSteps.createBuildConfig(projectile, repository);

        openShiftSteps.createJenkinsConfigMap(projectile, repository);

        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);

        gitSteps.pushToGitRepository(projectile, repository);

        // Trigger the build in Openshift
        openShiftSteps.triggerBuild(projectile);

        // Create Codebase in WIT
        String cheStack = buildConfig.getMetadata().getAnnotations().get(Annotations.CHE_STACK);
        witSteps.createCodebase(projectile.getSpace().getId(), cheStack, repository);

        return ImmutableBoom.builder()
                .createdRepository(repository)
                .createdProject(ImmutableOpenShiftProject.builder().name(projectile.getOpenShiftProjectName()).build())
                .build();
    }


    /**
     * Used in /osio/import
     */
    public Boom launchImport(OsioImportProjectile projectile) {
        GitRepository repository = gitSteps.findRepository(projectile);

        BuildConfig buildConfig = openShiftSteps.createBuildConfig(projectile, repository);
        openShiftSteps.createJenkinsConfigMap(projectile, repository);

        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);

        // Trigger the build in Openshift
        openShiftSteps.triggerBuild(projectile);

        // Create Codebase in WIT
        String cheStack = buildConfig.getMetadata().getAnnotations().get(Annotations.CHE_STACK);
        witSteps.createCodebase(projectile.getSpace().getId(), cheStack, repository);

        return ImmutableBoom.builder()
                .createdRepository(repository)
                .createdProject(ImmutableOpenShiftProject.builder().name(projectile.getOpenShiftProjectName()).build())
                .build();
    }
}