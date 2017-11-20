package io.fabric8.launcher.core.impl.observers;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.StatusMessageEvent;
import io.fabric8.launcher.core.api.inject.Step;
import io.fabric8.launcher.core.impl.events.CreateProjectileEvent;
import io.fabric8.launcher.core.impl.MissionControlImpl;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.github.api.GitHubWebhook;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.core.api.StatusEventType.GITHUB_WEBHOOK;

/**
 * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
 */
@ApplicationScoped
public class GitHubWebhookStepObserver {
    private final Event<StatusMessageEvent> statusEvent;

    private final OpenShiftServiceFactory openShiftServiceFactory;

    private final OpenShiftClusterRegistry openShiftClusterRegistry;

    private final GitHubServiceFactory gitHubServiceFactory;

    @Inject
    public GitHubWebhookStepObserver(OpenShiftServiceFactory openShiftServiceFactory,
                                     OpenShiftClusterRegistry openShiftClusterRegistry,
                                     GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
        this.openShiftServiceFactory = openShiftServiceFactory;
        this.openShiftClusterRegistry = openShiftClusterRegistry;
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    public void execute(@Observes @Step(GITHUB_WEBHOOK) CreateProjectileEvent event) {
        assert event.getGitHubRepository() != null: "Github repository is not set";

        CreateProjectile projectile = event.getProjectile();
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());

        OpenShiftProject openShiftProject = openShiftService.findProject(projectile.getOpenShiftProjectName()).get();
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = event.getGitHubRepository();

        List<GitHubWebhook> webhooks = MissionControlImpl.getGitHubWebhooks(gitHubService, openShiftService, gitHubRepository, openShiftProject);
        event.setWebhooks(webhooks);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }

}
