package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.openshift.appdev.missioncontrol.base.EnvironmentSupport;
import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.IdentityVisitor;
import io.openshift.appdev.missioncontrol.base.identity.TokenIdentity;
import io.openshift.appdev.missioncontrol.base.identity.UserPasswordIdentity;
import io.openshift.appdev.missioncontrol.service.github.api.DuplicateWebhookException;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubUser;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhookEvent;
import io.openshift.appdev.missioncontrol.service.github.api.NoSuchRepositoryException;
import io.openshift.appdev.missioncontrol.service.github.api.NoSuchWebhookException;
import io.openshift.appdev.missioncontrol.service.github.spi.GitHubServiceSpi;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 * Implementation of {@link GitHubService} backed by the Kohsuke GitHub Java Client
 * http://github-api.kohsuke.org/
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class KohsukeGitHubServiceImpl implements GitHubService, GitHubServiceSpi {

    public static final String GITHUB_WEBHOOK_WEB = "web";

    /**
     * Creates a new instance with the specified, required delegate
     *
     * @param delegate
     */
    KohsukeGitHubServiceImpl(final GitHub delegate, final Identity identity) {
        assert delegate != null : "delegate must be specified";
        this.delegate = delegate;
        this.identity = identity;
    }

    private static final String WEBHOOK_CONFIG_PROP_INSECURE_SSL_NAME = "insecure_ssl";

    private static final String WEBHOOK_CONFIG_PROP_INSECURE_SSL_VALUE = "1";

    private static final Logger log = Logger.getLogger(KohsukeGitHubServiceImpl.class.getName());

    private static final String MSG_NOT_FOUND = "Not Found";

    private static final String WEBHOOK_URL = "url";

    private static final String LAUNCHPAD_MISSION_CONTROL_COMMITTER_AUTHOR = "LAUNCHPAD_MISSION_CONTROL_COMMITTER_AUTHOR";

    private static final String LAUNCHPAD_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL = "LAUNCHPAD_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL";

    private final GitHub delegate;

    private final Identity identity;

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public boolean repositoryExists(String repositoryName) {
        try {
            return this.delegate.getRepository(repositoryName) != null;
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (KohsukeGitHubServiceImpl.isRepoNotFound(ioe)) {
                return false;
            }
            throw new RuntimeException("Could not fork " + repositoryName, ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubRepository fork(final String repositoryFullName) throws NoSuchRepositoryException,
            IllegalArgumentException {
        // Precondition checks
        if (repositoryFullName == null || repositoryFullName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }

        // First get the source repo
        final GHRepository source;
        try {
            source = delegate.getRepository(repositoryFullName);
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (KohsukeGitHubServiceImpl.isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not fork specified repository "
                                                            + repositoryFullName + " because it could not be found.");
            }
            throw new RuntimeException("Could not fork " + repositoryFullName, ioe);
        }

        // Fork (with retries as something is wonky here)
        GHRepository newlyCreatedRepo = null;
        final int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                newlyCreatedRepo = source.fork();
                break;
            } catch (final IOException ioe) {
                log.info("Trying fork operation again: " + i + " due to: " + ioe.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("Interrupted while waiting for fork retry", e);
                }
            }
        }
        if (newlyCreatedRepo == null) {
            throw new IllegalStateException("Newly created repo must be assigned; programming error");
        }

        // Wrap in our API view and return
        final GitHubRepository wrapped = new KohsukeGitHubRepositoryImpl(newlyCreatedRepo);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Forked " + source.getFullName() + " as " + newlyCreatedRepo.getFullName() +
                    " available at " + newlyCreatedRepo.getGitTransportUrl());
        }
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubRepository createRepository(String repositoryName,
                                             String description) throws IllegalArgumentException {
        // Precondition checks
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("repository description must be specified");
        }

        GHRepository newlyCreatedRepo = null;
        try {
            newlyCreatedRepo = delegate.createRepository(repositoryName)
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
            final String repositoryFullName;
            try {
                repositoryFullName = delegate.getMyself().getLogin() + '/' + repositoryName;
            } catch (final IOException ioe) {
                throw new RuntimeException(ioe);
            }
            if (this.repositoryExists(repositoryFullName)) {
                // We good
                break;
            }
            if (counter == 10) {
                throw new IllegalStateException("Newly-created repository "
                                                        + repositoryFullName + " could not be found ");
            }
            log.finest("Couldn't find repository " + repositoryFullName +
                               " after creating; waiting and trying again...");
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException ie) {
                Thread.interrupted();
                throw new RuntimeException("Someone interrupted thread while finding newly-created repo", ie);
            }
        }

        // Wrap in our API view and return
        final GitHubRepository wrapped = new KohsukeGitHubRepositoryImpl(newlyCreatedRepo);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created " + newlyCreatedRepo.getFullName() + " available at "
                    + newlyCreatedRepo.getGitTransportUrl());
        }
        return wrapped;
    }

    @Override
    public void push(GitHubRepository gitHubRepository, File path) throws IllegalArgumentException {
        String author = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSION_CONTROL_COMMITTER_AUTHOR, "openshiftio-launchpad");
        String authorEmail = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL, "obsidian-leadership@redhat.com");
        try (Git repo = Git.init().setDirectory(path).call()) {
            repo.add().addFilepattern(".").call();
            repo.commit().setMessage("Initial commit")
                    .setAuthor(author, authorEmail)
                    .setCommitter(author, authorEmail)
                    .call();
            RemoteAddCommand add = repo.remoteAdd();
            add.setName("origin");
            add.setUri(new URIish(gitHubRepository.getGitCloneUri().toURL()));
            add.call();
            PushCommand pushCommand = repo.push();
            identity.accept(new IdentityVisitor() {
                @Override
                public void visit(TokenIdentity token) {
                    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token.getToken(), ""));
                }

                @Override
                public void visit(UserPasswordIdentity userPassword) {
                    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userPassword.getUsername(), userPassword.getPassword()));
                }
            });
            pushCommand.call();
        } catch (GitAPIException | MalformedURLException e) {
            throw new RuntimeException("An error occurred while creating the git repo", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubWebhook createWebhook(final GitHubRepository repository,
                                       final URL webhookUrl,
                                       final GitHubWebhookEvent... events)
            throws IllegalArgumentException {
        // Precondition checks
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        if (webhookUrl == null) {
            throw new IllegalArgumentException("webhook URL must be specified");
        }
        if (events == null || events.length == 0) {
            throw new IllegalArgumentException("at least one event must be specified");
        }
        log.info("Adding webhook at '" + webhookUrl.toExternalForm() + "' on repository '" + repository.getFullName() + "'");

        final GHRepository repo;
        try {
            repo = delegate.getRepository(repository.getFullName());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
        Map<String, String> configuration = new HashMap<>();
        configuration.put(WEBHOOK_URL, webhookUrl.toString());
        configuration.put("content_type", "json");
        configuration.put(WEBHOOK_CONFIG_PROP_INSECURE_SSL_NAME, WEBHOOK_CONFIG_PROP_INSECURE_SSL_VALUE);

        List<GHEvent> githubEvents = Stream.of(events).map(event -> GHEvent.valueOf(event.name())).collect(Collectors.toList());

        final GHHook webhook;
        try {
            webhook = repo.createHook(
                    GITHUB_WEBHOOK_WEB,
                    configuration,
                    githubEvents,
                    true);
        } catch (final IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                final FileNotFoundException fnfe = (FileNotFoundException) ioe;
                if (fnfe.getMessage().contains("Hook already exists on this repository")) {
                    throw DuplicateWebhookException.create(webhookUrl);
                }
            }
            throw new RuntimeException(ioe);
        }

        final GitHubWebhook githubWebhook = new KohsukeGitHubWebhook(webhook);
        return githubWebhook;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubWebhook getWebhook(final GitHubRepository repository,
                                    final URL url)
            throws IllegalArgumentException, NoSuchWebhookException {
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
        final GHHook found;
        try {
            found = hooks.stream().filter(hook -> hook.getConfig().get(WEBHOOK_URL).equals(url.toString())).findFirst().get();
        } catch (final NoSuchElementException snee) {
            throw NoSuchWebhookException.create(repository, url);
        }
        return new KohsukeGitHubWebhook(found);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteWebhook(final GitHubRepository repository, GitHubWebhook webhook) throws IllegalArgumentException {
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        if (webhook == null) {
            throw new IllegalArgumentException("webhook must be specified");
        }
        final GHRepository repo;
        try {
            repo = delegate.getRepository(repository.getFullName());

            for (GHHook hook : repo.getHooks()) {
                if (hook.getConfig().get(WEBHOOK_URL).equals(webhook.getUrl())) {
                    hook.delete();
                    break;
                }
            }
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not remove webhooks from specified repository "
                                                            + repository.getFullName() + " because it could not be found or there is no webhooks for that repository.");
            }
            throw new RuntimeException("Could not remove webhooks from " + repository.getFullName(), ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRepository(final GitHubRepository repository) throws IllegalArgumentException {
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
        try {
            final GHRepository repo = delegate.getRepository(repositoryName);
            log.fine("Deleting repo at " + repo.gitHttpTransportUrl());
            repo.delete();
        } catch (final IOException ioe) {
            log.log(Level.SEVERE, "Error while deleting repository " + repositoryName, ioe);
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not remove repository "
                                                            + repositoryName + " because it could not be found.");
            }
            throw new RuntimeException("Could not remove " + repositoryName, ioe);
        }
    }

    /**
     * Determines if the required {@link IOException} in question represents a repo
     * that can't be found
     *
     * @param ioe
     * @return
     */
    private static boolean isRepoNotFound(final IOException ioe) {
        assert ioe != null : "ioe is required";
        final boolean notFound = ioe.getClass() == FileNotFoundException.class &&
                ioe.getMessage().contains(MSG_NOT_FOUND);
        final Throwable cause = ioe.getCause();
        if (!notFound && cause != null && cause instanceof IOException) {
            return isRepoNotFound((IOException) cause);
        }
        return notFound;
    }


    @Override
    public GitHubUser getLoggedUser() {
        try {
            return new KohsukeGitHubUser(delegate.getMyself());
        } catch (IOException e) {
            throw new RuntimeException("Could not find information about the logged user", e);
        }
    }
}
