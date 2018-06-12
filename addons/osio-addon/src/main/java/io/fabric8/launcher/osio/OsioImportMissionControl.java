package io.fabric8.launcher.osio;

import java.nio.file.Path;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.osio.client.OsioWitClient;
import io.fabric8.launcher.osio.client.Space;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;
import io.fabric8.launcher.osio.steps.GitSteps;
import io.fabric8.launcher.osio.steps.OpenShiftSteps;
import io.fabric8.launcher.osio.steps.WitSteps;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftProject;
import io.fabric8.openshift.api.model.BuildConfig;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class OsioImportMissionControl implements MissionControl<OsioImportProjectileContext, OsioImportProjectile> {

    @Inject
    private GitSteps gitSteps;

    @Inject
    private OpenShiftSteps openShiftSteps;

    @Inject
    private WitSteps witSteps;

    @Inject
    private OsioWitClient witClient;

    @Inject
    private StatusMessageEventBroker eventBroker;

    @Inject
    private Instance<ProjectilePreparer> preparers;


    @Override
    public OsioImportProjectile prepare(OsioImportProjectileContext context) {
        final Space space = witClient.findSpaceById(context.getSpaceId())
                .orElseThrow(() -> new IllegalStateException("Context space not found: " + context.getSpaceId()));
        Path path = gitSteps.clone(context);
        for (ProjectilePreparer preparer : preparers) {
            preparer.prepare(path, null, context);
        }
        return ImmutableOsioImportProjectile.builder()
                .projectLocation(path)
                .gitOrganization(context.getGitOrganization())
                .gitRepositoryName(context.getGitRepository())
                .openShiftProjectName(context.getProjectName())
                .pipelineId(context.getPipelineId())
                .space(space)
                .eventConsumer(eventBroker::send)
                .build();
    }

    /**
     * Used in /osio/import
     */
    @Override
    public Boom launch(OsioImportProjectile projectile) {
        // Make sure that cd-github is created in Openshift
        openShiftSteps.ensureCDGithubSecretExists();

        final GitRepository repository = gitSteps.findRepository(projectile);

        final BuildConfig buildConfig = openShiftSteps.createBuildConfig(projectile, repository);

        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);

        // Push the changes to the imported repository
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
}
