package io.fabric8.launcher.service.bitbucket;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProvider;

import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;
import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.BITBUCKET;

@ApplicationScoped
@GitProvider(BITBUCKET)
public class BitbucketServiceFactory implements GitServiceFactory {

    @Override
    public String getName() {
        return "Bitbucket";
    }

    @Override
    public BitbucketService create() {
        return create(getDefaultIdentity()
                              .orElseThrow(() -> new IllegalStateException("Env var " + BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD + " is not set.")));
    }

    @Override
    public BitbucketService create(final Identity identity) {
        return new BitbucketService(identity);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        return Optional.ofNullable(getUsername())
                .map(u -> IdentityFactory.createFromUserPassword(u, getPassword()));
    }

    private String getUsername() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME);
    }

    private String getPassword() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD);
    }

}
