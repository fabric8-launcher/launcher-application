package io.fabric8.launcher.osio.steps;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_WEBHOOK;
import static io.fabric8.launcher.osio.OsioConfigs.getJenkinsWebhookUrl;
import static io.fabric8.utils.Strings.notEmpty;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class GitSteps {
    private static final Logger log = Logger.getLogger(GitSteps.class.getName());

    @Inject
    private GitService gitService;

    public Path clone(OsioImportProjectileContext context) {
        GitRepository repository = findRepository(context.getGitOrganization(), context.getGitRepository());
        try {
            Path imported = Files.createTempDirectory("imported");
            return gitService.clone(repository, imported);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while creating temp directory", e);
        }
    }

    public GitRepository createRepository(OsioLaunchProjectile projectile) {
        final String repositoryName = Objects.toString(projectile.getGitRepositoryName(), projectile.getOpenShiftProjectName());
        final String repositoryDescription = projectile.getGitRepositoryDescription();
        final GitRepository gitRepository;
        if (notEmpty(projectile.getGitOrganization())) {
            gitRepository = gitService.createRepository(ImmutableGitOrganization.of(projectile.getGitOrganization()), repositoryName, repositoryDescription);
        } else {
            gitRepository = gitService.createRepository(repositoryName, repositoryDescription);
        }
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_CREATE,
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
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_PUSHED));
    }

    /**
     * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
     */
    public void createWebHooks(OsioProjectile projectile, GitRepository gitRepository) {
        final String jenkinsWebhookUrl = getJenkinsWebhookUrl();
        try {
            URL webhookUrl = new URL(jenkinsWebhookUrl);
            gitService.getHook(gitRepository, webhookUrl)
                    .orElseGet(() -> gitService.createHook(gitRepository, UUID.randomUUID().toString(), webhookUrl));
        } catch (final DuplicateHookException dpe) {
            // Swallow, it's OK, we've already forked this repo
            log.log(Level.FINE, dpe.getMessage(), dpe);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Malformed URL: " + jenkinsWebhookUrl, e);
            throw new IllegalStateException("Malformed webhook URL: " + jenkinsWebhookUrl, e);
        }
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }

    public GitRepository findRepository(OsioProjectile projectile) {
        return findRepository(projectile.getGitOrganization(), projectile.getGitRepositoryName());
    }

    private GitRepository findRepository(String organization, String repositoryName) {
        if (notEmpty(organization)) {
            final ImmutableGitOrganization gitOrganization = ImmutableGitOrganization.of(organization);
            return gitService.getRepository(gitOrganization, repositoryName)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("repository not found '%s/%s'", organization, repositoryName)));
        }
        return gitService.getRepository(repositoryName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("repository not found '%s'", repositoryName)));
    }
}