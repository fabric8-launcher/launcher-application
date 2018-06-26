package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.client.AnalyticsClient;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import okhttp3.FormBody;
import org.apache.maven.model.Dependency;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;

@Dependent
public class AnalyticsSteps {

    @Inject
    private AnalyticsClient analytics;

    public void pushToGithubRepository(OsioLaunchProjectile projectile) {
        FormBody.Builder requestBody = new FormBody.Builder();
        for (Dependency dep : projectile.projectDependencies()) {
            if (dep.getVersion() != null) {
                requestBody.add("dependency", dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
            } else {
                requestBody.add("dependency", dep.getGroupId() + ":" + dep.getArtifactId());
            }
        }
        requestBody.add("gitRepository", projectile.getGitRepositoryName());

        if (projectile.getGitOrganization() != null) {
            requestBody.add("gitOrganization", projectile.getGitOrganization());
        }

        boolean response = analytics.analyticsRequest("/api/v1/empty-booster", requestBody.build());
        if (response) {
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
        }
    }
}
