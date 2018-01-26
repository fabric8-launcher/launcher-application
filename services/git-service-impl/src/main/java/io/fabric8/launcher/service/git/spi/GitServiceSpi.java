package io.fabric8.launcher.service.git.spi;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;

/**
 * SPI on top of GitHubService to provide with operations that are not exposed
 * in the base API (e.g., for testing purpose).
 */
public interface GitServiceSpi extends GitService {

    /**
     * Delete a repository specified by its value object representation.
     *
     * @param repository - the value object the represents the GitHub repository
     * @throws IllegalArgumentException
     */
    default void deleteRepository(final GitRepository repository) throws IllegalArgumentException {
        Objects.requireNonNull(repository, "GitRepository cannot be null");
        deleteRepository(repository.getFullName());
    }

    /**
     * Delete a repository specified by its full name.
     *
     * @param repositoryName - GitHub repository name
     * @throws IllegalArgumentException
     */
    void deleteRepository(String repositoryName) throws IllegalArgumentException;

    /**
     * Returns the webhook with the specified url on the specified repository
     *
     * @param repository
     * @param url
     * @return
     * @throws IllegalArgumentException If either the repository or name are not specified
     */
    Optional<GitHook> getWebhook(GitRepository repository, URL url)
            throws IllegalArgumentException;
}
