package io.fabric8.launcher.core.impl.observers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.StatusMessageEvent;
import io.fabric8.launcher.core.api.inject.Step;
import io.fabric8.launcher.core.impl.events.CreateProjectileEvent;
import io.fabric8.launcher.service.github.api.DuplicateWebhookException;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.github.api.GitHubWebhook;
import io.fabric8.launcher.service.github.api.GitHubWebhookEvent;
import io.fabric8.launcher.service.github.spi.GitHubServiceSpi;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import org.apache.commons.lang.text.StrSubstitutor;

import static io.fabric8.launcher.core.api.StatusEventType.GITHUB_CREATE;
import static io.fabric8.launcher.core.api.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.StatusEventType.GITHUB_WEBHOOK;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
class GitHubStepObserver {
    @Inject
    public GitHubStepObserver(GitHubServiceFactory gitHubServiceFactory, OpenShiftServiceFactory openShiftServiceFactory, OpenShiftClusterRegistry openShiftClusterRegistry, Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
        this.gitHubServiceFactory = gitHubServiceFactory;
        this.openShiftServiceFactory = openShiftServiceFactory;
        this.openShiftClusterRegistry = openShiftClusterRegistry;
    }

    private final GitHubServiceFactory gitHubServiceFactory;

    private final OpenShiftServiceFactory openShiftServiceFactory;

    private final OpenShiftClusterRegistry openShiftClusterRegistry;

    private final Event<StatusMessageEvent> statusEvent;

    private Logger log = Logger.getLogger(GitHubStepObserver.class.getName());

    public void createGitHubRepository(@Observes @Step(GITHUB_CREATE) CreateProjectileEvent event) {
        assert event.getGitHubRepository() == null : "Github repository is already set";

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

    public void pushToGitHubRepository(@Observes @Step(GITHUB_PUSHED) CreateProjectileEvent event) {
        assert event.getGitHubRepository() != null : "Github repository is not set";

        CreateProjectile projectile = event.getProjectile();
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = event.getGitHubRepository();
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

    /**
     * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
     */
    public void createWebHooks(@Observes @Step(GITHUB_WEBHOOK) CreateProjectileEvent event) {
        assert event.getGitHubRepository() != null : "Github repository is not set";

        CreateProjectile projectile = event.getProjectile();
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());

        OpenShiftProject openShiftProject = openShiftService.findProject(projectile.getOpenShiftProjectName()).get();
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = event.getGitHubRepository();

        List<GitHubWebhook> webhooks = new ArrayList<>();
        for (URL webhookUrl : openShiftService.getWebhookUrls(openShiftProject)) {
            GitHubWebhook gitHubWebhook;
            try {
                gitHubWebhook = gitHubService.createWebhook(gitHubRepository, webhookUrl, GitHubWebhookEvent.PUSH);
            } catch (final DuplicateWebhookException dpe) {
                // Swallow, it's OK, we've already forked this repo
                log.log(Level.FINE, dpe.getMessage(), dpe);
                gitHubWebhook = ((GitHubServiceSpi) gitHubService).getWebhook(gitHubRepository, webhookUrl);
            }
            webhooks.add(gitHubWebhook);
        }
        event.setWebhooks(webhooks);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), GITHUB_WEBHOOK));
    }
}