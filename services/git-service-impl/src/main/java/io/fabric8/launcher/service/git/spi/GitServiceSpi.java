package io.fabric8.launcher.service.git.spi;

import java.util.Objects;

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
     * @param repositoryFullName - GitHub repository name
     * @throws IllegalArgumentException
     */
    void deleteRepository(String repositoryFullName) throws IllegalArgumentException;

}
