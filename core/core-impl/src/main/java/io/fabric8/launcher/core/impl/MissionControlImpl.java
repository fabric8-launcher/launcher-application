package io.fabric8.launcher.core.impl;

import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.DefaultMissionControl;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.projectiles.context.CreateProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;
import io.fabric8.launcher.core.impl.catalog.RhoarBoosterCatalogFactory;
import io.fabric8.launcher.core.impl.steps.GitSteps;
import io.fabric8.launcher.core.impl.steps.OpenShiftSteps;
import io.fabric8.launcher.core.spi.ProjectileEnricher;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link MissionControl} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Dependent
public class MissionControlImpl implements DefaultMissionControl {

    private static final Logger logger = Logger.getLogger(MissionControlImpl.class.getName());

    @Inject
    Instance<ProjectilePreparer> preparers;

    @Inject
    Instance<ProjectileEnricher> enrichers;

    @Inject
    Instance<GitSteps> gitStepsInstance;

    @Inject
    Instance<OpenShiftSteps> openShiftStepsInstance;

    @Inject
    Instance<TokenIdentity> identityInstance;

    @Inject
    RhoarBoosterCatalogFactory catalogFactory;


    @Override
    public CreateProjectile prepare(CreateProjectileContext context) {
        java.nio.file.Path path;
        try {
            path = Files.createTempDirectory("projectDir");
            // Wait for index to finish before querying the catalog
            catalogFactory.waitForIndex();
            RhoarBoosterCatalog catalog = catalogFactory.getBoosterCatalog();
            RhoarBooster booster = catalog.getBooster(context.getMission(), context.getRuntime(), context.getRuntimeVersion())
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Booster not found in catalog: %s-%s-%s ", context.getMission(), context.getRuntime(), context.getRuntimeVersion())));

            catalog.copy(booster, path);

            preparers.forEach(preparer -> preparer.prepare(path, booster, context));

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
    public Boom launch(CreateProjectile projectile) {
        GitSteps gitSteps = gitStepsInstance.get();
        OpenShiftSteps openShiftSteps = openShiftStepsInstance.get();

        try {
            enrichers.forEach(enricher -> enricher.accept(projectile));

            GitRepository gitRepository = null;
            // If the git repository name was not provided, do not create/push to git repository
            if (projectile.getGitRepositoryName() != null) {
                gitRepository = gitSteps.createGitRepository(projectile);
                gitSteps.pushToGitRepository(projectile, gitRepository);
            }

            OpenShiftProject openShiftProject = openShiftSteps.createOpenShiftProject(projectile);
            openShiftSteps.configureBuildPipeline(projectile, openShiftProject, gitRepository);

            if (gitRepository != null) {
                List<URL> webhooks = openShiftSteps.getWebhooks(openShiftProject);
                gitSteps.createWebHooks(projectile, gitRepository, webhooks);
            }

            return ImmutableBoom
                    .builder()
                    .createdProject(openShiftProject)
                    .createdRepository(gitRepository)
                    .build();
        } finally {
            gitStepsInstance.destroy(gitSteps);
            openShiftStepsInstance.destroy(openShiftSteps);
        }
    }
}
