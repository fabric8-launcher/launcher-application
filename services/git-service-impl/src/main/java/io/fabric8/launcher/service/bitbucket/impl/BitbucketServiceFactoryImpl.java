package io.fabric8.launcher.service.bitbucket.impl;

import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;

import java.util.Optional;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.bitbucket.api.BitbucketService;
import io.fabric8.launcher.service.bitbucket.api.BitbucketServiceFactory;

public class BitbucketServiceFactoryImpl implements BitbucketServiceFactory {
    @Override
    public BitbucketService create(final Identity identity) {
        return new BitbucketServiceImpl(identity);
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
