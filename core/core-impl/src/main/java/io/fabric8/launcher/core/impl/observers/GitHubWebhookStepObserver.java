package io.fabric8.launcher.core.impl.observers;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.StatusMessageEvent;
import io.fabric8.launcher.core.api.inject.Step;
import io.fabric8.launcher.core.impl.MissionControlImpl;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
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

    public void execute(@Observes @Step(GITHUB_WEBHOOK) CreateProjectile projectile) {
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());

        OpenShiftProject openShiftProject = openShiftService.findProject(projectile.getOpenShiftProjectName()).get();
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.getRepository(projectile.getGitHubRepositoryName());

        MissionControlImpl.getGitHubWebhooks(gitHubService, openShiftService, gitHubRepository, openShiftProject);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }

}
