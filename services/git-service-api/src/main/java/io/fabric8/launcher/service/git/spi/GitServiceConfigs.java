package io.fabric8.launcher.service.git.spi;

import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.service.git.api.GitServiceConfig;

public interface GitServiceConfigs {

    /**
     * @return A list of the configured git providers
     */
    List<GitServiceConfig> list();

    /**
     * @param id the Git provider unique id
     * @return the {@link GitServiceConfig} object
     */
    Optional<GitServiceConfig> findById(String id);

    /**
     * @return the {@link GitServiceConfig} object
     */
    GitServiceConfig defaultConfig();

}
