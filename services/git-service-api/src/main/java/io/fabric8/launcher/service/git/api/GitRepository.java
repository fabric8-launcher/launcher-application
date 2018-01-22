package io.fabric8.launcher.service.git.api;

import java.net.URI;

import org.immutables.value.Value;

/**
 * Value object representing a repository in Git
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface GitRepository {

    /**
     * @return the full repository name in form "owner/repoName"
     */
    String getFullName();

    /**
     * @return the github.com page for the repository
     */
    URI getHomepage();

    /**
     * @return the {@link URI} to use to clone the project from GitHub
     */
    URI getGitCloneUri();
}
