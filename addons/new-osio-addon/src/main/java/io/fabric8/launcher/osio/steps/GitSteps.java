package io.fabric8.launcher.osio.steps;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.utils.URLUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_WEBHOOK;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitSteps {
    private Logger log = Logger.getLogger(GitSteps.class.getName());

    @Inject
    private GitService gitService;

    @Inject
    private OpenshiftClient openshiftClient;

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


    public void createWebHooks(OsioProjectile projectile, GitRepository repository) {
        if (projectile.getStartOfStep() <= StatusEventType.GITHUB_WEBHOOK.ordinal()) {
            String jenkinsUrl = openshiftClient.getJenkinsUrl();
            try {
                URL webhookUrl = new URL(URLUtils.pathJoin(jenkinsUrl, "/github-webhook/"));
                gitService.createHook(repository, null, webhookUrl, gitService.getSuggestedNewHookEvents());
            } catch (final DuplicateHookException dpe) {
                // Swallow, it's OK, we've already forked this repo
                log.log(Level.FINE, dpe.getMessage(), dpe);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("openshift configured jenkins url is invalid", e);
            }
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }

    public void pushToGitRepository(OsioProjectile projectile, GitRepository repository) {
        if (projectile.getStartOfStep() <= StatusEventType.GITHUB_PUSHED.ordinal()) {
            Path projectLocation = projectile.getProjectLocation();

            // Add logged user in README.adoc
            Path readmeAdocPath = projectLocation.resolve("README.adoc");
            if (Files.exists(readmeAdocPath)) {
                try {
                    String content = new String(Files.readAllBytes(readmeAdocPath));
                    Map<String, String> values = new HashMap<>();
                    values.put("loggedUser", gitService.getLoggedUser().getLogin());
                    String newContent = new StrSubstitutor(values).replace(content);
                    Files.write(readmeAdocPath, newContent.getBytes());
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error while replacing README.adoc variables", e);
                }
            }

            gitService.push(repository, projectLocation);
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
    }
}
