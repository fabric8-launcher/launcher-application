package io.fabric8.launcher.service.gitlab.api;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;

/**
 * A factory for the {@link GitLabService} instance.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitLabServiceFactory {
    /**
     * Creates a new {@link GitLabService} with the default authentication.
     *
     * @return the created {@link GitLabService}
     */
    default GitLabService create() {
        return create(getDefaultIdentity().get());
    }

    /**
     * Creates a new {@link GitLabService} with the specified,
     * required personal access token.
     *
     * @param identity
     * @return the created {@link GitLabService}
     * @throws IllegalArgumentException If the {@code githubToken} is not specified
     */
    GitLabService create(Identity identity);

    /**
     * Returns the default identity for the Github service
     *
     * @return an optional {@link Identity}
     */
    Optional<Identity> getDefaultIdentity();

}
