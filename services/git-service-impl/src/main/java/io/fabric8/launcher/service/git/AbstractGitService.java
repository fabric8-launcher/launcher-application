package io.fabric8.launcher.service.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public abstract class AbstractGitService implements GitServiceSpi {

    protected AbstractGitService(final Identity identity) {
        this.identity = identity;
    }

    private static final Logger logger = Logger.getLogger(AbstractGitService.class.getName());

    private static final String AUTHOR = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR", "openshiftio-launchpad");

    private static final String AUTHOR_EMAIL = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL", "obsidian-leadership@redhat.com");

    private final Identity identity;

    @Override
    public Path clone(GitRepository repository, Path path) {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(path, "path must not be null.");
        // Not using JGit here because it doesn't support shallow clones yet
        ProcessBuilder builder = new ProcessBuilder()
                .command("git", "clone", repository.getGitCloneUri().toString(),
                         "--recursive",
                         "--depth=1",
                         "--quiet",
                         "-c", "advice.detachedHead=false",
                         path.toString())
                .inheritIO();
        logger.fine(() -> "Executing: " + builder.command().stream().collect(Collectors.joining(" ")));
        try {
            int exitCode = builder.start().waitFor();
            assert exitCode == 0 : "Process returned exit code: " + exitCode;
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted cloning process");
        } catch (IOException e) {
            throw new UncheckedIOException("Error while executing " +
                                                   builder.command().stream().collect(Collectors.joining(" ")), e);
        }
        return path;
    }

    public void push(GitRepository repository, Path path) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(path, "path must not be null.");

        try (Git repo = Git.init().setDirectory(path.toFile()).call()) {
            repo.add().addFilepattern(".").call();
            repo.commit().setMessage("Initial commit")
                    .setAuthor(AUTHOR, AUTHOR_EMAIL)
                    .setCommitter(AUTHOR, AUTHOR_EMAIL)
                    .call();
            RemoteAddCommand add = repo.remoteAdd();
            add.setName("origin");
            add.setUri(new URIish(repository.getGitCloneUri().toURL()));
            add.call();
            PushCommand pushCommand = repo.push();
            pushCommand.setCredentialsProvider(getJGitCredentialsProvider());
            pushCommand.call();
        } catch (GitAPIException | MalformedURLException e) {
            throw new RuntimeException("An error occurred while pushing to the git repo", e);
        }
    }

    protected CredentialsProvider getJGitCredentialsProvider() {
        final AtomicReference<CredentialsProvider> ref = new AtomicReference<>();
        getIdentity().accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                ref.set(new UsernamePasswordCredentialsProvider(token.getToken(), ""));
            }

            @Override
            public void visit(UserPasswordIdentity userPassword) {
                ref.set(new UsernamePasswordCredentialsProvider(userPassword.getUsername(), userPassword.getPassword()));
            }
        });
        final CredentialsProvider credentialsProvider = ref.get();
        if (credentialsProvider == null) {
            throw new IllegalStateException("this IdentityVisitor should implement all kind of identities.");
        }
        return credentialsProvider;
    }

    protected Identity getIdentity() {
        return identity;
    }

    protected GitRepository waitForRepository(String repositoryFullName) {
        // Block until exists
        int counter = 0;
        while (true) {
            counter++;
            final Optional<GitRepository> repository = getRepository(repositoryFullName);
            if (repository.isPresent()) {
                return repository.get();
            }
            if (counter == 10) {
                throw new IllegalStateException("Newly-created repository "
                                                        + repositoryFullName + " could not be found ");
            }
            logger.finest("Couldn't find repository " + repositoryFullName +
                                  " after creating; waiting and trying again...");
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException ie) {
                Thread.interrupted();
                throw new RuntimeException("Someone interrupted thread while finding newly-created repo", ie);
            }
        }
    }
}
