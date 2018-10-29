package io.fabric8.launcher.service.git.bitbucket;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.ImmutableUserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvironment;
import io.fabric8.launcher.service.git.spi.GitProvider;

import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvironment.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvironment.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;
import static io.fabric8.launcher.service.git.spi.GitProviderType.BITBUCKET;

@ApplicationScoped
@GitProvider(BITBUCKET)
public class BitbucketServiceFactory implements GitServiceFactory {

    private final HttpClient httpClient;

    @Inject
    public BitbucketServiceFactory(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Bitbucket";
    }

    @Override
    public BitbucketService create() {
        return create(getDefaultIdentity()
                              .orElseThrow(() -> new IllegalStateException("Env var " + BitbucketEnvironment.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD + " is not set.")),
                      null);
    }

    @Override
    public BitbucketService create(final Identity identity, String login) {
        return new BitbucketService(identity, httpClient);
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

}
