package io.fabric8.launcher.core.impl.steps;

import java.io.IOException;
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

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import org.apache.commons.lang.text.StrSubstitutor;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_WEBHOOK;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitSteps {

    @Inject
    private GitService gitService;

    @Inject
    private OpenShiftService openShiftService;

    @Inject
    private Event<StatusMessageEvent> statusEvent;

    private Logger log = Logger.getLogger(GitSteps.class.getName());

    public GitRepository createGitRepository(CreateProjectile projectile) {
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

    public void pushToGitRepository(CreateProjectile projectile, GitRepository repository) {
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

    /**
     * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
     */
    public void createWebHooks(CreateProjectile projectile, OpenShiftProject openShiftProject, GitRepository gitRepository) {
        for (URL webhookUrl : openShiftService.getWebhookUrls(openShiftProject)) {
            try {
                gitService.createHook(gitRepository, webhookUrl, gitService.getSuggestedNewHookEvents());
            } catch (final DuplicateHookException dpe) {
                // Swallow, it's OK, we've already forked this repo
                log.log(Level.FINE, dpe.getMessage(), dpe);
            }
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }
}