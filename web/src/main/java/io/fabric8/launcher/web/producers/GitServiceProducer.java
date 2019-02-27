package io.fabric8.launcher.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitServiceConfigs;
import io.fabric8.launcher.service.git.spi.GitServiceFactories;

/**
 * Produces {@link GitService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class GitServiceProducer {

    /**
     * Request header value for the Git provider implementation to use
     */
    static final String GIT_PROVIDER_HEADER = "X-Git-Provider";

    /**
     * Request header value for the authentication to use against the Git provider chosen by the GIT_PROVIDER_HEADER value
     */
    static final String GIT_AUTHORIZATION_HEADER = "X-Git-Authorization";

    private final GitServiceFactories gitServiceFactories;

    private final GitServiceConfigs gitServiceConfigs;

    /**
     * Used in proxies
     */
    @Deprecated
    GitServiceProducer() {
        this.gitServiceFactories = null;
        this.gitServiceConfigs = null;
    }

    @Inject
    public GitServiceProducer(GitServiceFactories gitServiceFactories, GitServiceConfigs gitServiceConfigs) {
        this.gitServiceFactories = gitServiceFactories;
        this.gitServiceConfigs = gitServiceConfigs;
    }

    @Produces
    @RequestScoped
    GitService getGitService(final HttpServletRequest request,
                             final IdentityProvider identityProvider,
                             final TokenIdentity authorization) {
        GitServiceConfig gitServiceConfig = getGitServiceConfig(request);
        GitServiceFactory gitServiceFactory = gitServiceFactories.getGitServiceFactory(gitServiceConfig.getType());
        String provider = gitServiceFactory.getName().toLowerCase();
        String gitAuthHeader = request.getHeader(GIT_AUTHORIZATION_HEADER);
        final Identity identity;
        if (gitAuthHeader != null) {
            // Supports Bearer Authorization headers only
            identity = TokenIdentity.fromBearerAuthorizationHeader(gitAuthHeader);
        } else {
            identity = gitServiceFactory.getDefaultIdentity().orElseGet(() -> identityProvider.getIdentity(authorization, provider)
                    .orElseThrow(() -> new NotFoundException("Git token not found")));
        }
        String userName = (String) request.getAttribute("USER_NAME");
        return gitServiceFactory.create(identity, userName, gitServiceConfig);
    }

    /**
     * Check if X-Git-Provider header param is specified and use that, otherwise,
     * use the LAUNCHER_GIT_PROVIDER env param value. It fallbacks to GitHub if not specified
     *
     * @param request the request containing the required Git provider (as specified by the X-Git-Provider header).
     * @return a {@link GitServiceFactory} object
     */
    GitServiceConfig getGitServiceConfig(HttpServletRequest request) {
        GitServiceConfig config;
        String provider = request.getHeader(GIT_PROVIDER_HEADER);
        if (provider == null) {
            config = gitServiceConfigs.list().get(0);
        } else {
            config = gitServiceConfigs.findById(provider).orElseThrow(IllegalArgumentException::new);
        }
        return config;
    }
}