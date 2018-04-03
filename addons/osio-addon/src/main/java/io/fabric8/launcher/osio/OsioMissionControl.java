package io.fabric8.launcher.osio;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.client.api.OsioWitClient;
import io.fabric8.launcher.osio.client.api.Space;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectileContext;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectileContext;
import io.fabric8.launcher.osio.steps.GitSteps;
import io.fabric8.launcher.osio.steps.JenkinsSteps;
import io.fabric8.launcher.osio.steps.OpenShiftSteps;
import io.fabric8.launcher.osio.steps.WitSteps;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftProject;
import io.fabric8.openshift.api.model.BuildConfig;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(OSIO)
@Dependent
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
    private OsioWitClient witClient;

    @Inject
    private JenkinsSteps jenkinsSteps;


    @Override
    public OsioLaunchProjectile prepare(ProjectileContext genericContext) {
        if (!(genericContext instanceof OsioProjectileContext)) {
            throw new IllegalArgumentException("OsioMissionControl only supports " + OsioProjectileContext.class.getName() + " instances");
        }
        final OsioProjectileContext context = (OsioProjectileContext) genericContext;
        final Projectile projectile = missionControl.prepare(context);
        final Space space = witClient.findSpaceById(context.getSpaceId());
        return ImmutableOsioLaunchProjectile.builder()
                .from(projectile)
                .gitOrganization(context.getGitOrganization())
                .space(space)
                .pipelineId(context.getPipelineId())
                .build();
    }

    public OsioImportProjectile prepareImport(OsioImportProjectileContext context) {
        final Space space = witClient.findSpaceById(context.getSpaceId());
        return ImmutableOsioImportProjectile.builder()
                .gitOrganization(context.getGitOrganization())
                .gitRepositoryName(context.getGitRepository())
                .openShiftProjectName(context.getProjectName())
                .pipelineId(context.getPipelineId())
                .space(space)
                .build();
    }

    @Override
    public Boom launch(Projectile genericProjectile) throws IllegalArgumentException {
        if (!(genericProjectile instanceof OsioLaunchProjectile)) {
            throw new IllegalArgumentException("OsioMissionControl only supports " + OsioLaunchProjectile.class.getName() + " instances");
        }
        final OsioLaunchProjectile projectile = (OsioLaunchProjectile) genericProjectile;
        final GitRepository repository = gitSteps.createRepository(projectile);
        jenkinsSteps.ensureJenkinsCDCredentialCreated();

        final BuildConfig buildConfig = openShiftSteps.createBuildConfig(projectile, repository);


        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);

        gitSteps.pushToGitRepository(projectile, repository);

        // Create jenkins config
        openShiftSteps.createJenkinsConfigMap(projectile, repository);

        // Trigger the build in Openshift
        openShiftSteps.triggerBuild(projectile);

        // Create Codebase in WIT
        final String cheStack = buildConfig.getMetadata().getAnnotations().get(Annotations.CHE_STACK);
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
        final GitRepository repository = gitSteps.findRepository(projectile);
        jenkinsSteps.ensureJenkinsCDCredentialCreated();

        final BuildConfig buildConfig = openShiftSteps.createBuildConfig(projectile, repository);
        openShiftSteps.createJenkinsConfigMap(projectile, repository);

        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);

        // Trigger the build in Openshift
        openShiftSteps.triggerBuild(projectile);

        // Create Codebase in WIT
        final String cheStack = buildConfig.getMetadata().getAnnotations().get(Annotations.CHE_STACK);
        witSteps.createCodebase(projectile.getSpace().getId(), cheStack, repository);

        return ImmutableBoom.builder()
                .createdRepository(repository)
                .createdProject(ImmutableOpenShiftProject.builder().name(projectile.getOpenShiftProjectName()).build())
                .build();
    }
}
