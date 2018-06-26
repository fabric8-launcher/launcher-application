package io.fabric8.launcher.osio;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.DefaultMissionControl;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.osio.client.OsioWitClient;
import io.fabric8.launcher.osio.client.Space;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;
import io.fabric8.launcher.osio.steps.AnalyticsSteps;
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
public class OsioLaunchMissionControl implements MissionControl<OsioProjectileContext, OsioLaunchProjectile> {

    @Inject
    private DefaultMissionControl missionControl;

    @Inject
    private GitSteps gitSteps;

    @Inject
    private OpenShiftSteps openShiftSteps;

    @Inject
    private WitSteps witSteps;

    @Inject
    private AnalyticsSteps analytics;

    @Inject
    private OsioWitClient witClient;

    @Inject
    private StatusMessageEventBroker eventBroker;

    @Override
    public OsioLaunchProjectile prepare(OsioProjectileContext context) {
        final Projectile projectile = missionControl.prepare(context);

        final Space space = witClient.findSpaceById(context.getSpaceId())
                .orElseThrow(() -> new IllegalStateException("Context space not found: " + context.getSpaceId()));
        return ImmutableOsioLaunchProjectile.builder()
                .from(projectile)
                .isEmptyRepository(context.isEmptyRepository())
                .projectDependencies(context.getDependencies())
                .space(space)
                .eventConsumer(eventBroker::send)
                .pipelineId(context.getPipelineId())
                .build();
    }

    @Override
    public Boom launch(OsioLaunchProjectile projectile) {
        // Make sure that cd-github is created in Openshift
        openShiftSteps.ensureCDGithubSecretExists();

        final GitRepository repository = gitSteps.createRepository(projectile);

        final BuildConfig buildConfig = openShiftSteps.createBuildConfig(projectile, repository);

        // create webhook first so that push will trigger build
        gitSteps.createWebHooks(projectile, repository);

        if (projectile.isEmptyRepository()) {
            analytics.pushToGithubRepository(projectile);
        } else {
            gitSteps.pushToGitRepository(projectile, repository);
        }
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
