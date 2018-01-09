package io.fabric8.launcher.core.impl.events;

import java.util.List;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CreateProjectileEvent {

    public CreateProjectileEvent(CreateProjectile projectile) {
        this.projectile = projectile;
    }

    private final CreateProjectile projectile;

    private GitRepository gitHubRepository;

    private OpenShiftProject openShiftProject;

    private List<GitHook> webhooks;

    public CreateProjectile getProjectile() {
        return projectile;
    }

    public GitRepository getGitHubRepository() {
        return gitHubRepository;
    }

    public void setGitHubRepository(GitRepository gitHubRepository) {
        this.gitHubRepository = gitHubRepository;
    }

    public OpenShiftProject getOpenShiftProject() {
        return openShiftProject;
    }

    public void setOpenShiftProject(OpenShiftProject openShiftProject) {
        this.openShiftProject = openShiftProject;
    }

    public List<GitHook> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(List<GitHook> webhooks) {
        this.webhooks = webhooks;
    }
}
