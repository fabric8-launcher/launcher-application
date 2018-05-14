package io.fabric8.launcher.core.impl.steps;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import org.apache.commons.text.StringSubstitutor;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_WEBHOOK;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class GitSteps {

    @Inject
    private GitService gitService;

    private static final Logger log = Logger.getLogger(GitSteps.class.getName());

    public GitRepository createGitRepository(CreateProjectile projectile) {
        GitRepository gitRepository;
        final String organizationName = projectile.getGitOrganization();
        final String repositoryName = Objects.toString(projectile.getGitRepositoryName(), projectile.getOpenShiftProjectName());
        if (projectile.getStartOfStep() > StatusEventType.GITHUB_CREATE.ordinal()) {
            // Do not create, just return the repository
            final String name = (organizationName != null ? organizationName + "/" : "") + repositoryName;
            gitRepository = gitService.getRepository(name)
                    .orElseThrow(() -> new IllegalArgumentException("Repository not found " + repositoryName));
        } else {
            final String repositoryDescription = projectile.getGitRepositoryDescription();
            if (organizationName != null) {
                gitRepository = gitService.createRepository(ImmutableGitOrganization.of(organizationName), repositoryName, repositoryDescription);
            } else {
                gitRepository = gitService.createRepository(repositoryName, repositoryDescription);
            }
        }
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_CREATE,
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
                    String newContent = new StringSubstitutor(values).replace(content);
                    Files.write(readmeAdocPath, newContent.getBytes());
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error while replacing README.adoc variables", e);
                }
            }

            gitService.push(repository, projectLocation);
        }
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
    }

    /**
     * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
     */
    public void createWebHooks(CreateProjectile projectile, GitRepository gitRepository, List<URL> webhooks) {
        for (URL webhookUrl : webhooks) {
            try {
                gitService.createHook(gitRepository, null, webhookUrl);
            } catch (final DuplicateHookException dpe) {
                // Swallow, it's OK, we've already forked this repo
                log.log(Level.FINE, dpe.getMessage(), dpe);
            }
        }
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }
}