package io.openshift.appdev.missioncontrol.service.github.api;

import java.net.URI;

/**
 * Value object representing a repository in GitHub
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface GitHubRepository {

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
