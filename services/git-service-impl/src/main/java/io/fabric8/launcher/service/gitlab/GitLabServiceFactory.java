package io.fabric8.launcher.service.gitlab;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProvider;
import io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames;

import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.GITLAB;
import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@GitProvider(GITLAB)
public class GitLabServiceFactory implements GitServiceFactory {

    @Override
    public String getName() {
        return "GitLab";
    }

    /**
     * Creates a new {@link GitLabService} with the default authentication.
     *
     * @return the created {@link GitLabService}
     */
    @Override
    public GitLabService create() {
        return create(getDefaultIdentity()
                              .orElseThrow(() -> new IllegalStateException("Env var " + GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN + " is not set.")));
    }

    @Override
    public GitLabService create(Identity identity) {
        if (!(identity instanceof TokenIdentity)) {
            throw new IllegalArgumentException("GitLabService supports only TokenIdentity. Not supported:" + identity);
        }
        return new GitLabService((TokenIdentity) identity);
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
