package io.fabric8.launcher.service.git.api;

import java.io.File;
import java.net.URL;

import io.fabric8.launcher.service.github.api.DuplicateWebhookException;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubWebhook;

/**
 * Defines the operations we support with the GitHub backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitService {

    /**
     * Creates a repository with the given information (name and description). The repository will be
     * created by default with no homepage, issues, wiki downloads and will be public.
     *
     * @param repositoryName - the name of the repository
     * @param description    - the repository description
     * @return the created {@link GitHubRepository}
     * @throws IllegalArgumentException
     */
    GitRepository createRepository(String repositoryName, String description) throws IllegalArgumentException;

    /**
     * Pushes to a repository. All files specified by the path will be added and pushed.
     *
     * @param repository - the repository to push to
     * @param path       - the files to be added and pushed
     * @throws IllegalArgumentException
     */
    void push(GitRepository repository, File path) throws IllegalArgumentException;

    /**
     * Checks if the repository with the given name exists
     *
     * @param repositoryName
     * @return <code>true</code> if it exists, <code>false</code> otherwise.
     */
    boolean repositoryExists(String repositoryName);

    /**
     * @return the user logged in this {@link GitService}
     */
    GitUser getLoggedUser();

    /**
     * @return the {@link GitRepository} if it exists. <code>null</code> otherwise
     */
    GitRepository getRepository(String repositoryName);
}