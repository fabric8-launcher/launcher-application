package io.fabric8.launcher.service.gitlab.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import io.fabric8.launcher.service.gitlab.api.GitLabServiceFactory;

import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.GITLAB_PRIVATE_TOKEN;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class GitLabServiceFactoryImpl implements GitLabServiceFactory {

    @Override
    public GitLabService create(Identity identity) {
        return new GitLabServiceImpl(identity);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        // Try using the provided Github token
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(GITLAB_PRIVATE_TOKEN);
        return token == null ? Optional.empty() : Optional.of(IdentityFactory.createFromToken(token));
    }
}
