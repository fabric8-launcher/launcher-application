package io.fabric8.launcher.service.gitlab.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import io.fabric8.launcher.service.gitlab.api.GitLabServiceFactory;

import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class GitLabServiceFactoryImpl implements GitLabServiceFactory {

    @Override
    public GitLabService create(Identity identity) {
        if (!(identity instanceof TokenIdentity)) {
            throw new IllegalArgumentException("GitLabService supports only TokenIdentity. Not supported:" + identity);
        }
        return new GitLabServiceImpl((TokenIdentity) identity);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        // Try using the provided Gitlab token
        return Optional.ofNullable(getToken())
                .map(t -> IdentityFactory.createFromToken("Private-Token", t));
    }

    private String getToken() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN);
    }
}
