package io.openshift.appdev.missioncontrol.service.github.spi;

import java.net.URL;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.github.api.NoSuchWebhookException;

/**
 * SPI on top of GitHubService to provide with operations that are not exposed
 * in the base API (e.g., for testing purpose).
 */
public interface GitHubServiceSpi extends GitHubService {

    /**
     * Delete a repository specified by its value object representation.
     *
     * @param repository - the value object the represents the GitHub repository
     * @throws IllegalArgumentException
     */
    void deleteRepository(final GitHubRepository repository) throws IllegalArgumentException;

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
     * @throws NoSuchWebhookException   If the webhook does not exist for this repo
     */
    GitHubWebhook getWebhook(GitHubRepository repository, URL url)
            throws IllegalArgumentException, NoSuchWebhookException;

    /**
     * Deletes a webhook in a specific GitHub repository
     *
     * @param repository - the value object that represents the GitHub repository
     * @param webhook    - the value object that represents the GitHub webhook
     * @throws IllegalArgumentException If either parameter is unspecified
     */
    void deleteWebhook(final GitHubRepository repository, GitHubWebhook webhook) throws IllegalArgumentException;

}
