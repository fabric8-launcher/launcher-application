package io.fabric8.launcher.core.impl.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.StatusMessageEvent;
import io.fabric8.launcher.core.api.inject.Step;
import io.fabric8.launcher.core.impl.events.CreateProjectileEvent;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;

import static io.fabric8.launcher.core.api.StatusEventType.GITHUB_CREATE;
import static java.util.Collections.singletonMap;

/**
 * Command that creates a github repo
 */
@ApplicationScoped
public class GitHubCreateStepObserver {

    @Inject
    public GitHubCreateStepObserver(GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    private final Event<StatusMessageEvent> statusEvent;

    private GitHubServiceFactory gitHubServiceFactory;

    public void execute(@Observes @Step(GITHUB_CREATE) CreateProjectileEvent event) {
        assert event.getGitHubRepository() == null: "Github repository is already set";

        CreateProjectile projectile = event.getProjectile();
        String repositoryDescription = projectile.getGitHubRepositoryDescription();
        String repositoryName = projectile.getGitHubRepositoryName();
        if (repositoryName == null) {
            repositoryName = projectile.getOpenShiftProjectName();
        }

        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.createRepository(repositoryName, repositoryDescription);
        event.setGitHubRepository(gitHubRepository);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_CREATE,
                                                singletonMap("location", gitHubRepository.getHomepage())));
    }
}
