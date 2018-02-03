package io.fabric8.launcher.core.impl;

import java.io.IOException;
import java.nio.file.Files;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.ImmutableProjectile;
import io.fabric8.launcher.core.api.LauncherProjectileContext;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.impl.steps.GitSteps;
import io.fabric8.launcher.core.impl.steps.OpenShiftSteps;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.tracking.SegmentAnalyticsProvider;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;

/**
 * Implementation of the {@link MissionControl} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Application(LAUNCHER)
@RequestScoped
public class MissionControlImpl implements MissionControl {

    @Inject
    private Instance<ProjectilePreparer> preparers;

    @Inject
    private Instance<GitSteps> gitStepsInstance;

    @Inject
    private Instance<OpenShiftSteps> openShiftStepsInstance;

    @Inject
    private SegmentAnalyticsProvider analyticsProvider;

    @Inject
    private RhoarBoosterCatalog catalog;

    @Override
    public Projectile prepare(ProjectileContext context) {
        java.nio.file.Path path;
        try {
            path = Files.createTempDirectory("projectDir");
            RhoarBooster booster = catalog.getBooster(context.getMission(), context.getRuntime(), context.getRuntimeVersion())
                    .orElseThrow(IllegalArgumentException::new);

            catalog.copy(booster, path);

            for (ProjectilePreparer preparer : preparers) {
                preparer.prepare(path, booster, context);
            }

            ImmutableProjectile.Builder builder = ImmutableProjectile.builder()
                    .projectLocation(path)
                    .mission(context.getMission())
                    .runtime(context.getRuntime());

            if (context instanceof LauncherProjectileContext) {
                LauncherProjectileContext launcherContext = (LauncherProjectileContext) context;
                builder.openShiftProjectName(launcherContext.getProjectName())
                        .gitRepositoryName(launcherContext.getGitRepository());
            } else {
                // Add placeholders for required attributes
                builder.openShiftProjectName("project").gitRepositoryName("repository");
            }
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException("Error while preparing projectile", e);
        }
    }

    @Override
    public Boom launch(Projectile projectile) throws IllegalArgumentException {
        int startIndex = projectile.getStartOfStep();
        assert startIndex >= 0 : "startOfStep cannot be negative. Was " + startIndex;

        GitSteps gitSteps = gitStepsInstance.get();
        OpenShiftSteps openShiftSteps = openShiftStepsInstance.get();

        // TODO: Use startIndex
        GitRepository gitRepository = gitSteps.createGitRepository(projectile);
        gitSteps.pushToGitRepository(projectile, gitRepository);

        OpenShiftProject openShiftProject = openShiftSteps.createOpenShiftProject(projectile);
        openShiftSteps.configureBuildPipeline(projectile, openShiftProject, gitRepository);

        gitSteps.createWebHooks(projectile, openShiftProject, gitRepository);

        // Call analytics
        analyticsProvider.trackingMessage(projectile);

        return ImmutableBoom
                .builder()
                .createdProject(openShiftProject)
                .createdRepository(gitRepository)
                .build();
    }
}
