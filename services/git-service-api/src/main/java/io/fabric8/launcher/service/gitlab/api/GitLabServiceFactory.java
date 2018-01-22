package io.fabric8.launcher.service.gitlab.api;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;

/**
 * A factory for the {@link GitLabService} instance.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitLabServiceFactory extends GitServiceFactory {
    /**
     * Creates a new {@link GitLabService} with the default authentication.
     *
     * @return the created {@link GitLabService}
     */
    @Override
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
    @Override
    GitLabService create(Identity identity);
}
