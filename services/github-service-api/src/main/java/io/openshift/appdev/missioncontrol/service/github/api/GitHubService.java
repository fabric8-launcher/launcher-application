package io.openshift.appdev.missioncontrol.service.github.api;

import java.io.File;
import java.net.URL;

/**
 * Defines the operations we support with the GitHub backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface GitHubService {

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

    /**
     * Creates a repository with the given information (name and description). The repository will be
     * created by default with no homepage, issues, wiki downloads and will be public.
     *
     * @param repositoryName - the name of the repository
     * @param description    - the repository description
     * @return the created {@link GitHubRepository}
     * @throws IllegalArgumentException
     */
    GitHubRepository createRepository(String repositoryName, String description) throws IllegalArgumentException;

    /**
     * Pushes to a repository. All files specified by the path will be added and pushed.
     *
     * @param repository - the repository to push to
     * @param path       - the files to be added and pushed
     * @throws IllegalArgumentException
     */
    void push(GitHubRepository repository, File path) throws IllegalArgumentException;

    /**
     * Creates a webhook in the GitHub repository.
     *
     * @param repository - the value object that represents the GitHub repository
     * @param webhookUrl - the URL of the webhook
     * @param events     - the events that trigger the webhook; at least one is required
     * @return the created {@link GitHubWebhook}
     * @throws IllegalArgumentException  If any of the parameters are unspecified
     * @throws DuplicateWebhookException If the webhook alreday exists
     */
    GitHubWebhook createWebhook(GitHubRepository repository,
                                URL webhookUrl,
                                GitHubWebhookEvent... events)
            throws IllegalArgumentException, DuplicateWebhookException;

    /**
     * Checks if the repository with the given name exists
     *
     * @param repositoryName
     * @return <code>true</code> if it exists, <code>false</code> otherwise.
     */
    boolean repositoryExists(String repositoryName);

    /**
     * @return the user logged in this {@link GitHubService}
     */
    GitHubUser getLoggedUser();
}