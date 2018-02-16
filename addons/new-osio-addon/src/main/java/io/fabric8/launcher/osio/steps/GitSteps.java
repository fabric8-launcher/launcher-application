package io.fabric8.launcher.osio.steps;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_CREATE;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitSteps {

    @Inject
    private GitService gitService;

    @Inject
    private Event<StatusMessageEvent> statusEvent;

    public GitRepository createRepository(OsioProjectile projectile) {
        GitRepository gitRepository;
        final String repositoryName = Objects.toString(projectile.getGitRepositoryName(), projectile.getOpenShiftProjectName());
        if (projectile.getStartOfStep() > StatusEventType.GITHUB_CREATE.ordinal()) {
            // Do not create, just return the repository
            gitRepository = gitService.getRepository(repositoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Repository not found " + repositoryName));
        } else {
            final String repositoryDescription = projectile.getGitRepositoryDescription();
            gitRepository = gitService.createRepository(repositoryName, repositoryDescription);
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_CREATE,
                                                singletonMap("location", gitRepository.getHomepage())));
        return gitRepository;
    }


}
