package io.fabric8.launcher.service.git.api;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.github.api.GitHubService;

/**
 * A Service Factory for {@link GitService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitServiceFactory {

    /**
     * @return a human-readable name that this service provides
     */
    String getName();

    /**
     * Creates a new {@link GitHubService} with the default authentication.
     *
     * @return the created {@link GitHubService}
     */
    default GitService create() {
        return create(getDefaultIdentity().get());
    }

    /**
     * Creates a new {@link GitHubService} with the specified,
     * required personal access token.
     *
     * @param identity
     * @return the created {@link GitHubService}
     * @throws IllegalArgumentException If the {@code githubToken} is not specified
     */
    GitService create(Identity identity);


    /**
     * Returns the default identity for the Github service
     *
     * @return an optional {@link Identity}
     */
    Optional<Identity> getDefaultIdentity();


}
