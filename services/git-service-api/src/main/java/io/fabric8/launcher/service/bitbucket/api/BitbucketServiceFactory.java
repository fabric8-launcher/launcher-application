package io.fabric8.launcher.service.bitbucket.api;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;

/**
 * A factory for the {@link BitbucketService} instance.
 *
 */
public interface BitbucketServiceFactory extends GitServiceFactory {

    @Override
    default String getName() {
        return "Bitbucket";
    }

    /**
     * Creates a new {@link BitbucketService} with the default authentication.
     *
     * @return the created {@link BitbucketService}
     */
    @Override
    default BitbucketService create() {
        return create(getDefaultIdentity()
                              .orElseThrow(() -> new IllegalStateException("Env var " + BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD + " is not set.")));
    }

    /**
     * Creates a new {@link BitbucketService} with the specified,
     * required personal access token.
     *
     * @param identity
     * @return the created {@link BitbucketService}
     * @throws IllegalArgumentException If the {@code githubToken} is not specified
     */
    @Override
    BitbucketService create(Identity identity);
}
