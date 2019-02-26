package io.fabric8.launcher.service.git.bitbucket;

import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.ImmutableUserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.api.ImmutableGitServiceConfig;
import io.fabric8.launcher.service.git.spi.GitProvider;

import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvironment.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvironment.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;
import static io.fabric8.launcher.service.git.spi.GitProviderType.BITBUCKET;

@ApplicationScoped
@GitProvider(BITBUCKET)
public class BitbucketServiceFactory implements GitServiceFactory {

    private static final GitServiceConfig DEFAULT_CONFIG = ImmutableGitServiceConfig.builder()
            .id("BitBucket")
            .name("BitBucket")
            .apiUrl("https://api.bitbucket.org")
            .repositoryUrl("https://bitbucket.org")
            .type(BITBUCKET)
            .build();

    /**
     * Lazy initialization
     */
    private final Supplier<HttpClient> httpClient;

    /**
     * Used in tests and proxies
     */
    public BitbucketServiceFactory() {
        this.httpClient = HttpClient::create;
    }

    @Inject
    public BitbucketServiceFactory(HttpClient httpClient) {
        this.httpClient = () -> httpClient;
    }

    @Override
    public GitService create(Identity identity, String login, GitServiceConfig config) {
        return new BitbucketService(identity, config.getApiUrl(), httpClient.get());
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        return Optional.ofNullable(getUsername())
                .map(u -> ImmutableUserPasswordIdentity.of(u, getPassword()));
    }

    private String getUsername() {
        return LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME.value();
    }

    private String getPassword() {
        return LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD.valueRequired();
    }

    @Override
    public GitServiceConfig getDefaultConfig() {
        return DEFAULT_CONFIG;
    }
}
