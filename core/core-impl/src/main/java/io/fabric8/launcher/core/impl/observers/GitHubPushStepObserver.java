package io.fabric8.launcher.core.impl.observers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.StatusMessageEvent;
import io.fabric8.launcher.core.api.Step;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import org.apache.commons.lang.text.StrSubstitutor;

import static io.fabric8.launcher.core.api.StatusEventType.GITHUB_PUSHED;

/**
 * Command that creates a github repo
 */
@ApplicationScoped
public class GitHubPushStepObserver {

    private static final Logger log = Logger.getLogger(GitHubPushStepObserver.class.getName());
    private final GitHubServiceFactory gitHubServiceFactory;
    private final Event<StatusMessageEvent> statusEvent;

    @Inject
    GitHubPushStepObserver(GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    public void execute(@Observes @Step(GITHUB_PUSHED) CreateProjectile projectile) {
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.getRepository(projectile.getGitHubRepositoryName());
        File path = projectile.getProjectLocation().toFile();

        // Add logged user in README.adoc
        File readmeAdoc = new File(path, "README.adoc");
        if (readmeAdoc.exists()) {
            try {
                String content = new String(Files.readAllBytes(readmeAdoc.toPath()));
                Map<String, String> values = new HashMap<>();
                values.put("loggedUser", gitHubService.getLoggedUser().getLogin());
                String newContent = new StrSubstitutor(values).replace(content);
                Files.write(readmeAdoc.toPath(), newContent.getBytes());
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error while replacing README.adoc variables", e);
            }
        }

        gitHubService.push(gitHubRepository, path);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
    }
}
