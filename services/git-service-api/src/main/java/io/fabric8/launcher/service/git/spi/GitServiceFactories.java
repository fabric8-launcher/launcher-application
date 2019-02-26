package io.fabric8.launcher.service.git.spi;

import java.util.List;

import io.fabric8.launcher.service.git.api.GitServiceFactory;

/**
 * Operations related to {@link GitServiceFactory} instances
 */
public interface GitServiceFactories {

    /**
     * @param type the {@link GitProvider} type associated with this
     * @return the {@link GitServiceFactory} associated with the given {@link GitProviderType}
     */
    GitServiceFactory getGitServiceFactory(GitProviderType type);

    List<GitServiceFactory> getGitServiceFactories();

}
