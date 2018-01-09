package io.fabric8.launcher.service.github.api;

import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;

/**
 * Defines the operations we support with the GitHub backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface GitHubService extends GitService {

    /**
     * Forks the specified repository (in full name format "owner/repo") into the current user's namespace
     * and returns a reference to the
     * If the user already has a fork of the specified repository, a reference to it will be returned.
     *
     * @param repositoryFullName
     * @return The target repo forked from the source, or the user's existing fork of the source repo
     * @throws NoSuchRepositoryException If the specified repository does not exist
     * @throws IllegalArgumentException  If the repository name is not specified
     */
    GitHubRepository fork(String repositoryFullName) throws NoSuchRepositoryException,
            IllegalArgumentException;
}