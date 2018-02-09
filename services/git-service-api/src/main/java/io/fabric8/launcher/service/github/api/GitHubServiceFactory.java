package io.fabric8.launcher.service.github.api;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;

/**
 * A factory for the {@link GitHubService} instance.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public interface GitHubServiceFactory extends GitServiceFactory {

    @Override
    default String getName() {
        return "GitHub";
    }

    /**
     * Creates a new {@link GitHubService} with the default authentication.
     *
     * @return the created {@link GitHubService}
     */
    @Override
    default GitHubService create() {
        return create(getDefaultIdentity().orElseThrow(() -> new IllegalStateException("Env var " + GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN + " is not set.")));
    }

    /**
     * Creates a new {@link GitHubService} with the specified,
     * required personal access token.
     *
     * @param identity
     * @return the created {@link GitHubService}
     * @throws IllegalArgumentException If the {@code githubToken} is not specified
     */
    @Override
    GitHubService create(Identity identity);
}