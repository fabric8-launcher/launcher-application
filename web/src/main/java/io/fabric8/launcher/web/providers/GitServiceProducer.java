package io.fabric8.launcher.web.providers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.web.cdi.NamedLiteral;

/**
 * Produces {@link GitService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitServiceProducer {

    private static final String GIT_PROVIDER_HEADER = "X-Git-Provider";

    private static final String GIT_TOKEN_HEADER = "X-Git-Token";

    @Inject
    private Instance<GitServiceFactory> gitServiceFactories;

    @Inject
    private GitHubServiceFactory defaultProvider;

    @Produces
    @RequestScoped
    GitService getGitService(HttpServletRequest request, IdentityProvider identityProvider) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        GitServiceFactory gitServiceFactory = getGitServiceFactory(request);
        Identity identity;
        String token = request.getHeader(GIT_TOKEN_HEADER);
        if (token != null) {
            identity = IdentityFactory.createFromToken(token);
        } else {
            // TODO: The request can be from OSIO or Launcher. Read the X-Application header and find a strategy to grab the token
            identity = gitServiceFactory.getDefaultIdentity()
                    .orElseGet(
                            () -> identityProvider.getIdentity(gitServiceFactory.getName().toLowerCase(), authorization)
                                    .orElseThrow(NotFoundException::new)
                    );
        }
        return gitServiceFactory.create(identity);
    }

    private GitServiceFactory getGitServiceFactory(HttpServletRequest request) {
        GitServiceFactory result;
        String provider = request.getHeader(GIT_PROVIDER_HEADER);
        if (provider != null) {
            Instance<GitServiceFactory> instance = gitServiceFactories.select(NamedLiteral.of(provider));
            if (!instance.isAmbiguous() && !instance.isUnsatisfied()) {
                result = instance.get();
            } else {
                throw new NotFoundException("Git provider not found: " + provider);
            }
        } else {
            result = defaultProvider;
        }
        return result;
    }

}