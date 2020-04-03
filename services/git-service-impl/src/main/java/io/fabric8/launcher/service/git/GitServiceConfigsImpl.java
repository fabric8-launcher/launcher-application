package io.fabric8.launcher.service.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.YamlUtils;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.spi.GitProviderType;
import io.fabric8.launcher.service.git.spi.GitServiceConfigs;
import io.fabric8.launcher.service.git.spi.GitServiceFactories;

import static io.fabric8.launcher.service.git.GitEnvironment.LAUNCHER_GIT_PROVIDER;
import static io.fabric8.launcher.service.git.spi.GitProviderType.GITHUB;
import static java.lang.String.format;

@ApplicationScoped
public class GitServiceConfigsImpl implements GitServiceConfigs {

    final List<GitServiceConfig> serviceConfigs = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(GitServiceConfigsImpl.class.getName());


    @Inject
    GitServiceFactories gitServiceFactories;

    /**
     * The default git provider to use if the header is not specified
     */
    private static final GitProviderType DEFAULT_GIT_PROVIDER_TYPE = GitProviderType.valueOf(LAUNCHER_GIT_PROVIDER.value(GITHUB.name()).toUpperCase());

    @PostConstruct
    public void initialize() {
        String configFile = GitEnvironment.LAUNCHER_GIT_PROVIDERS_FILE.value();
        if (configFile != null) {
            Path configFilePath = Paths.get(configFile);
            if (configFilePath.toFile().isFile()) {
                try {
                    try (BufferedReader reader = Files.newBufferedReader(configFilePath)) {
                        List<GitServiceConfig> configs = YamlUtils.readList(reader, GitServiceConfig.class);
                        serviceConfigs.addAll(configs);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, format("Error while reading git provider files : %s", configFile), e);
                }
            } else {
                logger.log(Level.WARNING, "Git Provider file does not exist: {0}", configFile);
            }
        }
        if (serviceConfigs.isEmpty()) {
            // if git_providers.yaml does not exist, return only the default config
            serviceConfigs.add(gitServiceFactories.getGitServiceFactory(DEFAULT_GIT_PROVIDER_TYPE).getDefaultConfig());
        }
    }

    @Override
    public List<GitServiceConfig> list() {
        return serviceConfigs;
    }

    @Override
    public Optional<GitServiceConfig> findById(final String id) {
        return serviceConfigs.stream()
                .filter(c -> c.getId().equals(id))
                .findAny();
    }

    @Override
    public GitServiceConfig defaultConfig() {
        return serviceConfigs.stream()
                .filter(c -> c.getType().equals(DEFAULT_GIT_PROVIDER_TYPE))
                .findAny()
                .orElse(serviceConfigs.get(0));
    }
}