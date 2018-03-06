package io.fabric8.launcher.service.bitbucket;

import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;

import java.util.Optional;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;

public class BitbucketGitServiceFactory implements GitServiceFactory {

    @Override
    public String getName() {
        return "Bitbucket";
    }

    @Override
    public BitbucketGitService create() {
        return create(getDefaultIdentity()
                              .orElseThrow(() -> new IllegalStateException("Env var " + BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD + " is not set.")));
    }

    @Override
    public BitbucketGitService create(final Identity identity) {
        return new BitbucketGitService(identity);
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
