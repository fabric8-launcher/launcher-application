package io.fabric8.launcher.core.impl.events;

import java.util.List;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubWebhook;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CreateProjectileEvent {

    public CreateProjectileEvent(CreateProjectile projectile) {
        this.projectile = projectile;
    }

    private final CreateProjectile projectile;

    private GitHubRepository gitHubRepository;

    private OpenShiftProject openShiftProject;

    private List<GitHubWebhook> webhooks;

    public CreateProjectile getProjectile() {
        return projectile;
    }

    public GitHubRepository getGitHubRepository() {
        return gitHubRepository;
    }

    public void setGitHubRepository(GitHubRepository gitHubRepository) {
        this.gitHubRepository = gitHubRepository;
    }

    public OpenShiftProject getOpenShiftProject() {
        return openShiftProject;
    }

    public void setOpenShiftProject(OpenShiftProject openShiftProject) {
        this.openShiftProject = openShiftProject;
    }

    public List<GitHubWebhook> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(List<GitHubWebhook> webhooks) {
        this.webhooks = webhooks;
    }
}
