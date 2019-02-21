package io.fabric8.launcher.service.git.spi;

import io.fabric8.launcher.service.git.api.GitServiceFactory;

/**
 * Creates a {@link GitServiceFactory}
 */
public interface GitServiceFactories {

    /**
     * @param type the {@link GitProvider} type associated with this
     * @return the {@link GitServiceFactory} associated with the given {@link GitProviderType}
     */
    GitServiceFactory getGitServiceFactory(GitProviderType type);
}
