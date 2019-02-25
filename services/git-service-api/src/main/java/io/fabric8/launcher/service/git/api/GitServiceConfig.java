package io.fabric8.launcher.service.git.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.launcher.service.git.spi.GitProviderType;
import org.immutables.value.Value;

/**
 * A configuration for creating a GitService
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableGitServiceConfig.class)
@JsonSerialize(as = ImmutableGitServiceConfig.class)
@JsonIgnoreProperties(value = {"serverProperties"}, allowSetters = true)
public interface GitServiceConfig {

    /**
     * @return a unique identifier for this instance
     */
    String getId();

    /**
     * @return A human-friendly name for this instance
     */
    String getName();

    /**
     * @return The URL this instance is configured
     */
    String getApiUrl();

    /**
     * @return The repository URL this instance is configured
     */
    @Value.Default
    default String getRepositoryUrl() {
        return getApiUrl();
    }

    /**
     * @return The type this instance targets
     */
    GitProviderType getType();

    /**
     * @return properties used only in the Git Provider implementation
     */
    Map<String, String> getClientProperties();

    /**
     * @return properties used only in the Git Provider implementation
     */
    Map<String, String> getServerProperties();
}