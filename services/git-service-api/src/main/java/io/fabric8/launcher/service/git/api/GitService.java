package io.fabric8.launcher.service.git.api;

import javax.annotation.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Defines the operations we support with the Git backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitService {

    /**
     * @return the list of organizations that the logged user belongs to.
     */
    List<GitOrganization> getOrganizations();

    /**
     * Get repositories.
     *
     * @param filter the {@see GitRepositoryFilter} to filter repositories
     * @return the list of repositories according to the filter.
     */
    List<GitRepository> getRepositories(GitRepositoryFilter filter);

    /**
     * Creates a repository with the given information (name and description). The repository will be
     * created by default with no homepage, issues, wiki downloads and will be public.
     *
     * @param organization   - the {@link GitOrganization} to create this repository
     * @param repositoryName - the name of the repository
     * @param description    - the repository description
     * @return the created {@link GitRepository}
     * @throws NoSuchOrganizationException if the user does not belong to the organization or the organization does not exist
     */
    GitRepository createRepository(GitOrganization organization, String repositoryName, String description) throws IllegalArgumentException;

    /**
     * Creates a repository with the given information (name and description). The repository will be
     * created by default with no homepage, issues, wiki downloads and will be public.
     *
     * @param repositoryName - the name of the repository
     * @param description    - the repository description
     * @return the created {@link GitRepository}
     * @throws IllegalArgumentException
     */
    GitRepository createRepository(String repositoryName, String description) throws IllegalArgumentException;

    /**
     * Pushes to a repository. All files specified by the {@link Path} will be added and pushed.
     *
     * @param repository - the repository to push to
     * @param path       - the directory containing the files to be added and pushed
     * @throws IllegalArgumentException
     */
    void push(GitRepository repository, Path path) throws IllegalArgumentException;

    /**
     * @return the user logged in this {@link GitService}
     */
    GitUser getLoggedUser();

    /**
     * Get a repository:
     * - by its repository full name {owner}/{name}
     * - by its repository name, in which case it will look for an owned repository
     *
     * @param name the repository full name or just its name
     * @return the {@link GitRepository} specified as an {@link Optional} nullable object
     */
    Optional<GitRepository> getRepository(String name);

    /**
     * @return the {@link GitRepository} specified as an {@link Optional} nullable object
     * @throws NoSuchOrganizationException if the user does not belong to the organization or the organization does not exist
     */
    Optional<GitRepository> getRepository(GitOrganization organization, String repositoryName);

    /**
     * Creates a webhook in the Git repository.
     *
     * @param repository - the value object that represents the Git repository
     * @param secret     - give the choice to add a secret to the created webhook or leave null for no secret
     * @param webhookUrl - the URL of the webhook
     * @param events     - the events that trigger the webhook; if none specified, {@see getSuggestedNewHookEvents} is used by default.
     * @return the created {@link GitHook}
     * @throws IllegalArgumentException If any of the parameters are unspecified
     * @throws DuplicateHookException   If the webhook already exists
     */
    GitHook createHook(GitRepository repository,
                       @Nullable String secret,
                       URL webhookUrl,
                       String... events)
            throws IllegalArgumentException;

    /**
     * Returns the webhooks for the specified repository
     *
     * @param repository
     * @return
     * @throws IllegalArgumentException If either the repository or name are not specified
     */
    List<GitHook> getHooks(GitRepository repository) throws IllegalArgumentException;

    /**
     * Returns the webhook with the specified url on the specified repository
     *
     * @param repository
     * @param url
     * @return
     * @throws IllegalArgumentException If either the repository or name are not specified
     */
    Optional<GitHook> getHook(GitRepository repository, URL url)
            throws IllegalArgumentException;

    /**
     * Deletes a webhook in a specific GitHub repository
     *
     * @param repository - the value object that represents the GitHub repository
     * @param webhook    - the value object that represents the GitHub webhook
     * @throws IllegalArgumentException If either parameter is unspecified
     */
    void deleteWebhook(GitRepository repository, GitHook webhook) throws IllegalArgumentException;

    /**
     * The suggested events to be used during hook creation
     */
    String[] getSuggestedNewHookEvents();

}