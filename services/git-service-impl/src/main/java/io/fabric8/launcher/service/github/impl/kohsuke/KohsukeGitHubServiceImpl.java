package io.fabric8.launcher.service.github.impl.kohsuke;

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

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitHookEvent;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.NoSuchHookException;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.spi.GitHubServiceSpi;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHFileNotFoundException;
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

    private static final String WEBHOOK_URL = "url";

    private static final String LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR = "LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR";

    private static final String LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL = "LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL";

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
        } catch (final GHFileNotFoundException ghe) {
            return false;
        } catch (final IOException ioe) {
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
        } catch (final GHFileNotFoundException ghe) {
            throw new NoSuchRepositoryException("Could not fork specified repository "
                                                        + repositoryFullName + " because it could not be found.");
        } catch (final IOException ioe) {
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
    public GitRepository createRepository(String repositoryName,
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
    public GitHubRepository getRepository(String repositoryName) {
        // Precondition checks
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }

        try {
            String repositoryFullName = delegate.getMyself().getLogin() + '/' + repositoryName;
            return new KohsukeGitHubRepositoryImpl(delegate.getRepository(repositoryFullName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void push(GitRepository gitHubRepository, File path) throws IllegalArgumentException {
        String author = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR, "openshiftio-launchpad");
        String authorEmail = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL, "obsidian-leadership@redhat.com");
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


    @Override
    public GitHook createHook(GitRepository repository, URL webhookUrl, GitHookEvent... events) throws IllegalArgumentException {
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

        List<GHEvent> githubEvents = Stream.of(events).map(o -> GHEvent.valueOf(o.name())).collect(Collectors.toList());

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

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHook getWebhook(final GitRepository repository,
                              final URL url)
            throws IllegalArgumentException, NoSuchHookException {
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
            throw NoSuchHookException.create(repository, url);
        }
        return new KohsukeGitHubWebhook(found);
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
        try {
            repo = delegate.getRepository(repository.getFullName());

            for (GHHook hook : repo.getHooks()) {
                if (hook.getConfig().get(WEBHOOK_URL).equals(webhook.getUrl())) {
                    hook.delete();
                    break;
                }
            }
        } catch (final GHFileNotFoundException ghe) {
            throw new NoSuchRepositoryException("Could not remove webhooks from specified repository "
                                                        + repository.getFullName() + " because it could not be found or there is no webhooks for that repository.");
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not remove webhooks from " + repository.getFullName(), ioe);
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
        try {
            final GHRepository repo = delegate.getRepository(repositoryName);
            log.fine("Deleting repo at " + repo.gitHttpTransportUrl());
            repo.delete();
        } catch (final GHFileNotFoundException ghe) {
            log.log(Level.SEVERE, "Error while deleting repository " + repositoryName, ghe);
            throw new NoSuchRepositoryException("Could not remove repository "
                                                        + repositoryName + " because it could not be found.");
        } catch (final IOException ioe) {
            log.log(Level.SEVERE, "Error while deleting repository " + repositoryName, ioe);
            throw new RuntimeException("Could not remove " + repositoryName, ioe);
        }
    }

    @Override
    public GitUser getLoggedUser() {
        try {
            return new KohsukeGitHubUser(delegate.getMyself());
        } catch (IOException e) {
            throw new RuntimeException("Could not find information about the logged user", e);
        }
    }
}
