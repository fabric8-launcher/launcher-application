package io.fabric8.launcher.service.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import static io.fabric8.launcher.service.git.GitEnvironment.LAUNCHER_GIT_COMMITTER_AUTHOR;
import static io.fabric8.launcher.service.git.GitEnvironment.LAUNCHER_GIT_COMMITTER_AUTHOR_EMAIL;
import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public abstract class AbstractGitService implements GitServiceSpi {

    protected AbstractGitService(final Identity identity) {
        this.identity = identity;
    }

    private static final Logger logger = Logger.getLogger(AbstractGitService.class.getName());

    private static final String AUTHOR = LAUNCHER_GIT_COMMITTER_AUTHOR.value("openshiftio-launchpad");

    private static final String AUTHOR_EMAIL = LAUNCHER_GIT_COMMITTER_AUTHOR_EMAIL.value("obsidian-leadership@redhat.com");

    private final Identity identity;

    @Override
    public Path clone(GitRepository repository, Path path) {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(path, "path must not be null.");
        // Not using JGit here because it doesn't support shallow clones yet
        ProcessBuilder builder = new ProcessBuilder()
                .command("git", "clone", repository.getGitCloneUri().toString(),
                         "--quiet",
                         "-c", "advice.detachedHead=false",
                         path.toString())
                .inheritIO();
        logger.fine(() -> "Executing: " + String.join(" ", builder.command()));
        try {
            int exitCode = builder.start().waitFor();
            assert exitCode == 0 : "Process returned exit code: " + exitCode;
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted cloning process");
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new UncheckedIOException("Error while executing " +
                                                   String.join(" ", builder.command()), e);
        }
        return path;
    }

    public void push(GitRepository repository, Path path) {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(path, "path must not be null.");

        try (Git repo = Git.init().setDirectory(path.toFile()).call()) {
            repo.add().addFilepattern(".").call();
            repo.commit().setMessage("Initial commit")
                    .setAuthor(AUTHOR, AUTHOR_EMAIL)
                    .setCommitter(AUTHOR, AUTHOR_EMAIL)
                    .call();
            // Retry push if NoRemoteRepositoryException happens
            RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                    .handle(NoRemoteRepositoryException.class)
                    .withDelay(Duration.ofSeconds(3))
                    .withMaxRetries(3);
            Failsafe.with(retryPolicy)
                    .run(() -> {
                        PushCommand pushCommand = repo.push();
                        pushCommand.setRemote(repository.getGitCloneUri().toString());
                        setCredentialsProvider(pushCommand::setCredentialsProvider);
                        pushCommand.call();
                    });
        } catch (FailsafeException fse) {
            throw new IllegalStateException("An error occurred while pushing to the git repo", fse.getCause());
        } catch (GitAPIException e) {
            throw new IllegalStateException("An error occurred while pushing to the git repo", e);
        }
    }


    protected void setCredentialsProvider(Consumer<CredentialsProvider> consumer) {
        getIdentity().accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                consumer.accept(new UsernamePasswordCredentialsProvider(token.getToken(), ""));
            }

            @Override
            public void visit(UserPasswordIdentity userPassword) {
                consumer.accept(new UsernamePasswordCredentialsProvider(userPassword.getUsername(), userPassword.getPassword()));
            }
        });
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    protected GitRepository waitForRepository(String repositoryFullName) {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                .handleResult(null)
                .withDelay(Duration.ofSeconds(3))
                .withMaxRetries(5);

        GitRepository gitRepository = Failsafe.with(retryPolicy)
                .get(() -> getRepository(repositoryFullName).orElse(null));
        if (gitRepository == null) {
            throw new NoSuchRepositoryException("Repository not found: " + repositoryFullName);
        }
        return gitRepository;
    }
}
