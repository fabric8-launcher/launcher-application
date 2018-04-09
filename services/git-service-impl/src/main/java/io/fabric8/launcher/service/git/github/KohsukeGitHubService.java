package io.fabric8.launcher.service.git.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
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

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.AbstractGitService;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitRepositoryFilter;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.api.NoSuchOrganizationException;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.github.api.GitHubWebhookEvent;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GitHub;

import static io.fabric8.launcher.service.git.Gits.checkGitRepositoryFullNameArgument;
import static io.fabric8.launcher.service.git.Gits.checkGitRepositoryNameArgument;
import static io.fabric8.launcher.service.git.Gits.createGitRepositoryFullName;
import static io.fabric8.launcher.service.git.Gits.isValidGitRepositoryFullName;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Implementation of {@link GitService} backed by the Kohsuke GitHub Java Client
 * http://github-api.kohsuke.org/
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class KohsukeGitHubService extends AbstractGitService implements GitService {

    private static final String GITHUB_WEBHOOK_WEB = "web";

    /**
     * Creates a new instance with the specified, required delegate
     *
     * @param delegate the @{See GitHub} delegate
     * @param identity the @{See Identity}
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
                    .sorted(Comparator.comparing(GitOrganization::getName))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot fetch the organizations for this user", e);
        }
    }

    @Override
    public List<GitRepository> getRepositories(GitRepositoryFilter filter) {
        requireNonNull(filter, "filter must be specified.");

        final GHRepositorySearchBuilder searchBuilder = delegate.searchRepositories();
        if (filter.withOrganization() != null) {
            final String orgName = filter.withOrganization().getName();
            checkOrganizationExists(orgName);
            searchBuilder.q("org:" + orgName);
        } else {
            searchBuilder.user(getMyself().getLogin());
        }
        if (isNotEmpty(filter.withNameContaining())) {
            searchBuilder.q(filter.withNameContaining() + " in:name");
        }
        try {
            return searchBuilder.list().asList().stream()
                    .map(KohsukeGitHubRepository::new)
                    .collect(Collectors.toList());
        } catch (final GHException e) {
            // We catch exception because GitHub search api is returning an error when there is no result.
            // Therefore we have no way for now to make the difference between an error and an empty result.
            return Collections.emptyList();
        }
    }

    private GHMyself getMyself() {
        try {
            return delegate.getMyself();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot fetch myself", e);
        }
    }

    private GHOrganization checkOrganizationExists(final String name) {
        requireNonNull(name, "name must be specified.");
        try {
            return delegate.getOrganization(name);
        } catch (FileNotFoundException e) {
            throw new NoSuchOrganizationException("User does not belong to organization '" + name + "' or the organization does not exist");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot fetch the organization named " + name, e);
        }
    }

    @Override
    public GitRepository createRepository(GitOrganization organization, String repositoryName, String description) throws IllegalArgumentException {
        // Precondition checks
        checkGitRepositoryNameArgument(repositoryName);
        requireNonNull(description, "description must be specified.");
        if (description.isEmpty()) {
            throw new IllegalArgumentException("description must not be empty.");
        }

        GHRepository newlyCreatedRepo;
        try {
            GHCreateRepositoryBuilder repositoryBuilder;
            if (organization == null) {
                repositoryBuilder = delegate.createRepository(repositoryName);
            } else {
                GHOrganization ghOrganization = checkOrganizationExists(organization.getName());
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

        final GitRepository gitRepository = waitForRepository(newlyCreatedRepo.getFullName());
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created " + gitRepository.getFullName() + " available at "
                    + gitRepository.getGitCloneUri());
        }
        return gitRepository;
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
        requireNonNull(name, "name must be specified.");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("repositoryName must not be empty.");
        }

        try {
            if (isValidGitRepositoryFullName(name)) {
                return getRepositoryByFullName(name);
            } else {
                checkGitRepositoryNameArgument(name);
                return getRepositoryByFullName(createGitRepositoryFullName(delegate.getMyself().getLogin(), name));
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<GitRepository> getRepository(GitOrganization organization, String repositoryName) {
        requireNonNull(organization, "organization must be specified.");
        checkGitRepositoryNameArgument(repositoryName);

        checkOrganizationExists(organization.getName());

        return getRepositoryByFullName(createGitRepositoryFullName(organization.getName(), repositoryName));
    }

    private Optional<GitRepository> getRepositoryByFullName(final String repositoryFullName) {
        checkGitRepositoryFullNameArgument(repositoryFullName);

        try {
            GHRepository repository = delegate.getRepository(repositoryFullName);
            return repository == null ? Optional.empty() : Optional.of(new KohsukeGitHubRepository(repository));
        } catch (GHFileNotFoundException fnfe) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GitHook createHook(final GitRepository repository, final String secret, final URL webhookUrl, final String... events) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhookUrl, "webhookUrl must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        final String[] effectiveEvents = events != null && events.length > 0 ? events : getSuggestedNewHookEvents();

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

        List<GHEvent> githubEvents = Stream.of(effectiveEvents).map(e -> GHEvent.valueOf(e.toUpperCase(Locale.ENGLISH))).collect(Collectors.toList());

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
        requireNonNull(repository, "repository must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

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
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(url, "url must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

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
    public void deleteRepository(final String repositoryFullName) throws IllegalArgumentException {
        checkGitRepositoryFullNameArgument(repositoryFullName);

        getRepository(repositoryFullName).ifPresent((GitRepository gitRepository) -> {
            log.fine("Deleting repo at " + gitRepository.getGitCloneUri());
            try {
                delegate.getRepository(gitRepository.getFullName()).delete();
            } catch (final GHFileNotFoundException ghe) {
                log.log(Level.SEVERE, "Error while deleting repository " + repositoryFullName, ghe);
                throw new NoSuchRepositoryException("Could not remove repository " + repositoryFullName + " because it could not be found.");
            } catch (final IOException ioe) {
                log.log(Level.SEVERE, "Error while deleting repository " + repositoryFullName, ioe);
                throw new RuntimeException("Could not remove " + repositoryFullName, ioe);
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
        return new String[]{
                GitHubWebhookEvent.PUSH.id(),
                GitHubWebhookEvent.PULL_REQUEST.id(),
                GitHubWebhookEvent.ISSUE_COMMENT.id()
        };
    }
}
