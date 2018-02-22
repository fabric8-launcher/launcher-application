package io.fabric8.launcher.osio.steps;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_WEBHOOK;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitSteps {
    private static final Logger log = Logger.getLogger(GitSteps.class.getName());

    @Inject
    private GitService gitService;

    @Inject
    private Event<StatusMessageEvent> statusEvent;

    public GitRepository createRepository(OsioProjectile projectile) {
        GitRepository gitRepository;
        final String repositoryName = Objects.toString(projectile.getGitRepositoryName(), projectile.getOpenShiftProjectName());
        final String repositoryDescription = projectile.getGitRepositoryDescription();
        ImmutableGitOrganization gitOrganization = ImmutableGitOrganization.of(projectile.getGitOrganization());
        gitRepository = gitService.createRepository(gitOrganization, repositoryName, repositoryDescription);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_CREATE,
                                                singletonMap("location", gitRepository.getHomepage())));
        return gitRepository;
    }

    public void pushToGitRepository(OsioProjectile projectile, GitRepository repository) {
        if (projectile.getStartOfStep() <= StatusEventType.GITHUB_PUSHED.ordinal()) {
            Path projectLocation = projectile.getProjectLocation();

            // Add logged user in README.adoc
            Path readmeAdocPath = projectLocation.resolve("README.adoc");
            if (Files.exists(readmeAdocPath)) {
                try {
                    String content = new String(Files.readAllBytes(readmeAdocPath));
                    String newContent = content.replace("${loggedUser}", gitService.getLoggedUser().getLogin());
                    Files.write(readmeAdocPath, newContent.getBytes());
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error while replacing README.adoc variables", e);
                }
            }

            gitService.push(repository, projectLocation);
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
    }

    /**
     * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
     */
    public void createWebHooks(OsioProjectile projectile, GitRepository gitRepository) {
        String jenkinsWebhookURL = EnvironmentVariables.ExternalServices.getJenkinsWebhookURL();
        try {
            // TODO: Check if the webhook requires a secret
            gitService.createHook(gitRepository, null, new URL(jenkinsWebhookURL));
        } catch (final DuplicateHookException dpe) {
            // Swallow, it's OK, we've already forked this repo
            log.log(Level.FINE, dpe.getMessage(), dpe);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Malformed URL: " + jenkinsWebhookURL, e);
            throw new IllegalStateException("Malformed webhook URL: " + jenkinsWebhookURL, e);
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }

    public GitRepository findRepository(OsioProjectile projectile) {
        String repositoryName = projectile.getGitRepositoryName();
        ImmutableGitOrganization gitOrganization = ImmutableGitOrganization.of(projectile.getGitOrganization());

        return gitService.getRepository(gitOrganization, repositoryName).orElseThrow(IllegalArgumentException::new);
    }
}