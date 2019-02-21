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
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProviderType;
import io.fabric8.launcher.service.git.spi.GitServiceFactories;

import static io.fabric8.launcher.service.git.GitEnvironment.LAUNCHER_GIT_PROVIDER;
import static io.fabric8.launcher.service.git.spi.GitProviderType.GITHUB;
import static io.fabric8.launcher.service.git.spi.GitProviderType.valueOf;

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

    /**
     * The default git provider to use if the header is not specified
     */
    private static final String DEFAULT_GIT_PROVIDER = LAUNCHER_GIT_PROVIDER.value(GITHUB.name()).toUpperCase();

    private final GitServiceFactories gitServiceFactories;

    @Inject
    public GitServiceProducer(GitServiceFactories gitServiceFactories) {
        this.gitServiceFactories = gitServiceFactories;
    }

    @Produces
    @RequestScoped
    GitService getGitService(final HttpServletRequest request,
                             final IdentityProvider identityProvider,
                             final TokenIdentity authorization) {
        GitServiceFactory gitServiceFactory = getGitServiceFactory(request);
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
        return gitServiceFactory.create(identity, userName);
    }

    /**
     * Check if X-Git-Provider header param is specified and use that, otherwise,
     * use the LAUNCHER_GIT_PROVIDER env param value. It fallbacks to GitHub if not specified
     *
     * @param request the request containing the required Git provider (as specified by the X-Git-Provider header).
     * @return a {@link GitServiceFactory} object
     */
    GitServiceFactory getGitServiceFactory(HttpServletRequest request) {
        String provider = request.getHeader(GIT_PROVIDER_HEADER);
        if (provider == null) {
            provider = DEFAULT_GIT_PROVIDER;
        }
        GitProviderType type = valueOf(provider.toUpperCase());
        return gitServiceFactories.getGitServiceFactory(type);
    }
}