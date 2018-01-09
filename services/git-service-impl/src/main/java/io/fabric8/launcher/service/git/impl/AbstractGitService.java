package io.fabric8.launcher.service.git.impl;

import java.io.File;
import java.net.MalformedURLException;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class AbstractGitService {
    public AbstractGitService(final Identity identity) {
        this.identity = identity;
    }

    private static final String LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR = "LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR";

    private static final String LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL = "LAUNCHER_MISSION_CONTROL_COMMITTER_AUTHOR_EMAIL";

    protected final Identity identity;

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
}
