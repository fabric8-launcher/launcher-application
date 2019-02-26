package io.fabric8.launcher.service.git;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProvider;
import io.fabric8.launcher.service.git.spi.GitProviderType;
import io.fabric8.launcher.service.git.spi.GitServiceFactories;

import static java.util.stream.Collectors.toList;

/**
 * Uses CDI to lookup the correct {@link GitServiceFactory} implementation
 */
@ApplicationScoped
public class GitServiceFactoriesImpl implements GitServiceFactories {

    @Inject
    @Any
    Instance<GitServiceFactory> gitServiceFactories;

    @Override
    public GitServiceFactory getGitServiceFactory(GitProviderType type) {
        return gitServiceFactories.select(GitProvider.GitProviderLiteral.of(type)).get();
    }

    @Override
    public List<GitServiceFactory> getGitServiceFactories() {
        return gitServiceFactories.stream().collect(toList());
    }
}