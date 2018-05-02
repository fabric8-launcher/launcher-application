package io.fabric8.launcher.core.impl;

import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.projectiles.context.CreateProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;
import io.fabric8.launcher.core.impl.catalog.RhoarBoosterCatalogFactory;
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

    private static final Logger logger = Logger.getLogger(MissionControlImpl.class.getName());

    @Inject
    private Instance<ProjectilePreparer> preparers;

    @Inject
    private Instance<GitSteps> gitStepsInstance;

    @Inject
    private Instance<OpenShiftSteps> openShiftStepsInstance;

    @Inject
    private Instance<TokenIdentity> identityInstance;

    @Inject
    private SegmentAnalyticsProvider analyticsProvider;

    @Inject
    private RhoarBoosterCatalogFactory catalogFactory;


    @Override
    public CreateProjectile prepare(ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            throw new IllegalArgumentException("ProjectileContext should be a " + CreateProjectileContext.class.getName() + " instance");
        }

        CreateProjectileContext createContext = (CreateProjectileContext) context;
        java.nio.file.Path path;
        try {
            path = Files.createTempDirectory("projectDir");
            // Wait for index to finish before querying the catalog
            catalogFactory.waitForIndex();
            RhoarBoosterCatalog catalog = catalogFactory.getBoosterCatalog();
            RhoarBooster booster = catalog.getBooster(createContext.getMission(), createContext.getRuntime(), createContext.getRuntimeVersion())
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Booster not found in catalog: %s-%s-%s ", createContext.getMission(), createContext.getRuntime(), createContext.getRuntimeVersion())));

            catalog.copy(booster, path);

            for (ProjectilePreparer preparer : preparers) {
                preparer.prepare(path, booster, createContext);
            }

            ImmutableLauncherCreateProjectile.Builder builder = ImmutableLauncherCreateProjectile.builder()
                    .projectLocation(path)
                    .booster(booster);

            if (context instanceof LauncherProjectileContext) {
                LauncherProjectileContext launcherContext = (LauncherProjectileContext) context;
                builder.openShiftProjectName(launcherContext.getProjectName())
                        .gitOrganization(launcherContext.getGitOrganization())
                        .gitRepositoryName(launcherContext.getGitRepository());
            }
            return builder.build();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while preparing projectile: " + e.getMessage(), e);
            throw new IllegalStateException("Error while preparing projectile: " + e.getMessage(), e);
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

        List<URL> webhooks = openShiftSteps.getWebhooks(openShiftProject);
        gitSteps.createWebHooks(createProjectile, gitRepository, webhooks);

        // Call analytics
        analyticsProvider.trackingMessage(createProjectile, identityInstance.isUnsatisfied() ? null : identityInstance.get());

        return ImmutableBoom
                .builder()
                .createdProject(openShiftProject)
                .createdRepository(gitRepository)
                .build();
    }
}
