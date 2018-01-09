package io.fabric8.launcher.service.github.api;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;

/**
 * A factory for the {@link GitHubService} instance.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public interface GitHubServiceFactory {

    /**
     * Creates a new {@link GitHubService} with the default authentication.
     *
     * @return the created {@link GitHubService}
     */
    default GitHubService create() {
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
    GitHubService create(Identity identity);


    /**
     * Returns the default identity for the Github service
     *
     * @return an optional {@link Identity}
     */
    Optional<Identity> getDefaultIdentity();
}