package io.fabric8.launcher.core.impl;

import javax.inject.Inject;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.spi.GitOperations;
import io.fabric8.launcher.core.spi.OpenShiftOperations;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.tracking.SegmentAnalyticsProvider;

/**
 * Implementation of the {@link MissionControl} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class MissionControlImpl implements MissionControl {

    @Inject
    private GitOperations gitOperations;

    @Inject
    private OpenShiftOperations openShiftOperations;

    @Inject
    private SegmentAnalyticsProvider analyticsProvider;

    @Override
    public Boom launch(CreateProjectile projectile) throws IllegalArgumentException {
        int startIndex = projectile.getStartOfStep();
        assert startIndex >= 0 : "startOfStep cannot be negative. Was " + startIndex;

        // TODO: Use startIndex
        GitRepository gitRepository = gitOperations.createGitRepository(projectile);
        gitOperations.pushToGitRepository(projectile, gitRepository);

        OpenShiftProject openShiftProject = openShiftOperations.createOpenShiftProject(projectile);
        openShiftOperations.configureBuildPipeline(projectile, openShiftProject, gitRepository);

        gitOperations.createWebHooks(projectile, openShiftProject, gitRepository);

        // Call analytics
        analyticsProvider.trackingMessage(projectile);

        return ImmutableBoom
                .builder()
                .createdProject(openShiftProject)
                .createdRepository(gitRepository)
                .build();
    }

}
