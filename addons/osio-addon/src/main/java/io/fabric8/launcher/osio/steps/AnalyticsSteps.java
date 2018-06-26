package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.client.AnalyticsClient;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import okhttp3.MediaType;
import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;

@Dependent
public class AnalyticsSteps {

    @Inject
    private AnalyticsClient analytics;

    public void pushToGithubRepository(OsioLaunchProjectile projectile) {
        List<String> parameters = new ArrayList();
        final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");

        for (Dependency dep : projectile.projectDependencies()) {
            if (dep.getVersion() != null) {
                parameters.add("dependency=" + dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
            } else {
                parameters.add("dependency=" + dep.getGroupId() + ":" + dep.getArtifactId());
            }
        }
        parameters.add("gitRepository=" + projectile.getGitRepositoryName());

        if (projectile.getGitOrganization() != null) {
            parameters.add("gitOrganization=" + projectile.getGitOrganization());
        }
        String payload = String.join("&", parameters);
        analytics.Request("/api/v1/empty-booster", CONTENT_TYPE, payload);
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
    }
}
