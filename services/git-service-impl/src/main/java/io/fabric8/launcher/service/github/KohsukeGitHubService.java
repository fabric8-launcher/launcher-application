package io.fabric8.launcher.service.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.AbstractGitService;
import io.fabric8.launcher.service.github.api.GitHubWebhookEvent;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 * Implementation of {@link GitService} backed by the Kohsuke GitHub Java Client
 * http://github-api.kohsuke.org/
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class KohsukeGitHubService extends AbstractGitService implements GitService {

    public static final String GITHUB_WEBHOOK_WEB = "web";

    /**
     * Creates a new instance with the specified, required delegate
     *
     * @param delegate
     */
    KohsukeGitHubService(final GitHub delegate, final Identity identity) {
        super(identity);
        assert delegate != null : "delegate must be specified";
        this.delegate = delegate;
    }

    private static final String WEBHOOK_CONFIG_PROP_INSECURE_SSL_NAME = "insecure_ssl";

    private static final String WEBHOOK_CONFIG_PROP_SECRET = "secret";

    private static final String WEBHOOK_CONFIG_PROP_INSECURE_SSL_VALUE = "1";

    private static final Logger log = Logger.getLogger(KohsukeGitHubService.class.getName());

    private static final String WEBHOOK_URL = "url";

    private final GitHub delegate;

    @Override
    public List<GitOrganization> getOrganizations() {
        try {
            return delegate.getMyOrganizations().values()
                    .stream()
                    .map(o -> ImmutableGitOrganization.of(o.getLogin()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot fetch the organizations for this user", e);
        }
    }

    @Override
    public List<GitRepository> getRepositories(GitOrganization organization) {
        GHPerson person;
        try {
            if (organization != null) {
                try {
                    person = delegate.getOrganization(organization.getName());
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException("User does not belong to organization '" + organization.getName() + "' or the organization does not exist", e);
                }
            } else {
                person = delegate.getMyself();
            }
        } catch (IOException e) {
            String name = organization != null ? "organization '" + organization.getName() + "'" : "this user";
            throw new IllegalStateException("Cannot fetch the repositories for " + name, e);
        }
        return StreamSupport
                .stream(person.listRepositories().spliterator(), false)
                .map(KohsukeGitHubRepository::new)
                .collect(Collectors.toList());
    }

    @Override
    public GitRepository createRepository(GitOrganization organization, String repositoryName, String description) throws IllegalArgumentException {
        // Precondition checks
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("repository description must be specified");
        }

        GHRepository newlyCreatedRepo;
        try {
            GHCreateRepositoryBuilder repositoryBuilder;
            if (organization == null) {
                repositoryBuilder = delegate.createRepository(repositoryName);
            } else {
                GHOrganization ghOrganization = delegate.getOrganization(organization.getName());
                repositoryBuilder = ghOrganization.createRepository(repositoryName);
            }
            newlyCreatedRepo = repositoryBuilder
                    .description(description)
                    .private_(false)
                    .homepage("")
                    .issues(false)
                    .downloads(false)
                    .wiki(false)
                    .create();
        } catch (IOException e) {
            throw new RuntimeException("Could not create GitHub repository named '" + repositoryName + "'", e);
        }

        // Block until exists
        int counter = 0;
        while (true) {
            counter++;
            if (this.getRepository(repositoryName).isPresent()) {
                // We good
                break;
            }
            if (counter == 10) {
                final String repositoryFullName;
                try {
                    repositoryFullName = delegate.getMyself().getLogin() + '/' + repositoryName;
                } catch (final IOException ioe) {
                    throw new RuntimeException(ioe);
                }

                throw new IllegalStateException("Newly-created repository "
                                                        + repositoryFullName + " could not be found ");
            }
            log.finest("Couldn't find repository " + repositoryName +
                               " after creating; waiting and trying again...");
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException ie) {
                Thread.interrupted();
                throw new RuntimeException("Someone interrupted thread while finding newly-created repo", ie);
            }
        }

        // Wrap in our API view and return
        final GitRepository wrapped = new KohsukeGitHubRepository(newlyCreatedRepo);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created " + newlyCreatedRepo.getFullName() + " available at "
                    + newlyCreatedRepo.getGitTransportUrl());
        }
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitRepository createRepository(String repositoryName,
                                          String description) throws IllegalArgumentException {
        return createRepository(null, repositoryName, description);
    }

    @Override
    public Optional<GitRepository> getRepository(String name) {
        // Precondition checks
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        try {
            if (name.contains("/")) {
                String[] split = name.split("/");
                return getRepository(ImmutableGitOrganization.of(split[0]), split[1]);
            } else {
                return getRepository(ImmutableGitOrganization.of(delegate.getMyself().getLogin()), name);
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<GitRepository> getRepository(GitOrganization organization, String repositoryName) {
        // Precondition checks
        if (organization == null) {
            throw new IllegalArgumentException("organization must be specified");
        }
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        try {
            GHRepository repository = delegate.getRepository(organization.getName() + "/" + repositoryName);
            return repository == null ? Optional.empty() : Optional.of(new KohsukeGitHubRepository(repository));
        } catch (GHFileNotFoundException fnfe) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GitHook createHook(GitRepository repository, String secret, URL webhookUrl, String... events) throws IllegalArgumentException {
        // Precondition checks
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        if (webhookUrl == null) {
            throw new IllegalArgumentException("webhook URL must be specified");
        }
        if (events == null || events.length == 0) {
            events = getSuggestedNewHookEvents();
        }
        log.info("Adding webhook at '" + webhookUrl.toExternalForm() + "' on repository '" + repository.getFullName() + "'");

        final GHRepository repo;
        try {
            String repoName = repository.getFullName();
            if (!repoName.contains("/")) {
                repoName = delegate.getMyself().getLogin() + "/" + repoName;
            }
            repo = delegate.getRepository(repoName);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
        Map<String, String> configuration = new HashMap<>();
        configuration.put(WEBHOOK_URL, webhookUrl.toString());
        configuration.put("content_type", "json");
        configuration.put(WEBHOOK_CONFIG_PROP_INSECURE_SSL_NAME, WEBHOOK_CONFIG_PROP_INSECURE_SSL_VALUE);
        if (secret != null && secret.length() > 0) {
            configuration.put(WEBHOOK_CONFIG_PROP_SECRET, secret);
        }

        List<GHEvent> githubEvents = Stream.of(events).map(e -> GHEvent.valueOf(e.toUpperCase(Locale.ENGLISH))).collect(Collectors.toList());

        try {
            GHHook webhook = repo.createHook(
                    GITHUB_WEBHOOK_WEB,
                    configuration,
                    githubEvents,
                    true);
            return new KohsukeGitHubWebhook(webhook);
        } catch (final IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                final FileNotFoundException fnfe = (FileNotFoundException) ioe;
                if (fnfe.getMessage().contains("Hook already exists on this repository")) {
                    throw DuplicateHookException.create(webhookUrl);
                }
            }
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public List<GitHook> getHooks(GitRepository repository) throws IllegalArgumentException {
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        try {
            String repoName = repository.getFullName();
            if (!repoName.contains("/")) {
                repoName = delegate.getMyself().getLogin() + "/" + repoName;
            }
            return delegate.getRepository(repoName).getHooks()
                    .stream()
                    .map(KohsukeGitHubWebhook::new)
                    .collect(Collectors.toList());
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not get webhooks for repository " + repository.getFullName(), ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GitHook> getHook(final GitRepository repository,
                                     final URL url)
            throws IllegalArgumentException {
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        if (url == null) {
            throw new IllegalArgumentException("url must be specified");
        }
        final List<GHHook> hooks;
        try {
            hooks = delegate.getRepository(repository.getFullName()).getHooks();
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not get webhooks for repository " + repository.getFullName(), ioe);
        }
        try {
            return hooks.stream()
                    .filter(hook -> hook.getConfig().get(WEBHOOK_URL).equals(url.toString()))
                    .findFirst()
                    .map(KohsukeGitHubWebhook::new);
        } catch (final NoSuchElementException snee) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteWebhook(final GitRepository repository, GitHook webhook) throws IllegalArgumentException {
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        if (webhook == null) {
            throw new IllegalArgumentException("webhook must be specified");
        }
        final GHRepository repo;
        String repositoryFullName = repository.getFullName();
        try {
            if (!repositoryFullName.contains("/")) {
                repositoryFullName = delegate.getMyself().getLogin() + "/" + repositoryFullName;
            }
            repo = delegate.getRepository(repositoryFullName);

            for (GHHook hook : repo.getHooks()) {
                if (hook.getConfig().get(WEBHOOK_URL).equals(webhook.getUrl())) {
                    hook.delete();
                    break;
                }
            }
        } catch (final GHFileNotFoundException ghe) {
            throw new NoSuchRepositoryException("Could not remove webhooks from specified repository "
                                                        + repositoryFullName + " because it could not be found or there is no webhooks for that repository.");
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not remove webhooks from " + repositoryFullName, ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRepository(final GitRepository repository) throws IllegalArgumentException {
        deleteRepository(repository.getFullName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRepository(final String repositoryName) throws IllegalArgumentException {
        if (repositoryName == null) {
            throw new IllegalArgumentException("repositoryName must be specified");
        }

        getRepository(repositoryName).ifPresent((GitRepository gitRepository) -> {
            log.fine("Deleting repo at " + gitRepository.getGitCloneUri());
            try {
                delegate.getRepository(gitRepository.getFullName()).delete();
            } catch (final GHFileNotFoundException ghe) {
                log.log(Level.SEVERE, "Error while deleting repository " + repositoryName, ghe);
                throw new NoSuchRepositoryException("Could not remove repository " + repositoryName + " because it could not be found.");
            } catch (final IOException ioe) {
                log.log(Level.SEVERE, "Error while deleting repository " + repositoryName, ioe);
                throw new RuntimeException("Could not remove " + repositoryName, ioe);
            }
        });

    }

    @Override
    public GitUser getLoggedUser() {
        try {
            GHMyself myself = delegate.getMyself();
            return ImmutableGitUser.of(myself.getLogin(),
                                       myself.getAvatarUrl());
        } catch (IOException e) {
            throw new RuntimeException("Could not find information about the logged user", e);
        }
    }

    @Override
    public String[] getSuggestedNewHookEvents() {
        String[] events = {
                GitHubWebhookEvent.PUSH.name(),
                GitHubWebhookEvent.PULL_REQUEST.name(),
                GitHubWebhookEvent.ISSUE_COMMENT.name()
        };
        return events;
    }
}
