package io.fabric8.launcher.core.impl;

import java.io.IOException;
import java.nio.file.Files;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.ImmutableLauncherCreateProjectile;
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
@Dependent
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
    public CreateProjectile prepare(ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            throw new IllegalArgumentException("ProjectileContext should be a " + CreateProjectileContext.class.getName() + " instance");
        }

        CreateProjectileContext createContext = (CreateProjectileContext) context;
        java.nio.file.Path path;
        try {
            path = Files.createTempDirectory("projectDir");
            RhoarBooster booster = catalog.getBooster(createContext.getMission(), createContext.getRuntime(), createContext.getRuntimeVersion())
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Booster not found in catalog: %s-%s-%s ", createContext.getMission(), createContext.getRuntime(), createContext.getRuntimeVersion())));

            catalog.copy(booster, path);

            for (ProjectilePreparer preparer : preparers) {
                preparer.prepare(path, booster, createContext);
            }

            ImmutableLauncherCreateProjectile.Builder builder = ImmutableLauncherCreateProjectile.builder()
                    .projectLocation(path)
                    .mission(createContext.getMission())
                    .runtime(createContext.getRuntime());

            if (context instanceof LauncherProjectileContext) {
                LauncherProjectileContext launcherContext = (LauncherProjectileContext) context;
                builder.openShiftProjectName(launcherContext.getProjectName())
                        .gitRepositoryName(launcherContext.getGitRepository());
            }
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException("Error while preparing projectile", e);
        }
    }

    @Override
    public Boom launch(Projectile projectile) throws IllegalArgumentException {
        if (!(projectile instanceof CreateProjectile)) {
            throw new IllegalArgumentException("Projectile should be a " + CreateProjectile.class.getName() + " instance");
        }
        CreateProjectile createProjectile = (CreateProjectile) projectile;
        int startIndex = projectile.getStartOfStep();
        assert startIndex >= 0 : "startOfStep cannot be negative. Was " + startIndex;

        GitSteps gitSteps = gitStepsInstance.get();
        OpenShiftSteps openShiftSteps = openShiftStepsInstance.get();

        GitRepository gitRepository = gitSteps.createGitRepository(createProjectile);
        gitSteps.pushToGitRepository(createProjectile, gitRepository);

        OpenShiftProject openShiftProject = openShiftSteps.createOpenShiftProject(createProjectile);
        openShiftSteps.configureBuildPipeline(createProjectile, openShiftProject, gitRepository);

        gitSteps.createWebHooks(createProjectile, openShiftProject, gitRepository);

        // Call analytics
        analyticsProvider.trackingMessage(createProjectile);

        return ImmutableBoom
                .builder()
                .createdProject(openShiftProject)
                .createdRepository(gitRepository)
                .build();
    }
}
