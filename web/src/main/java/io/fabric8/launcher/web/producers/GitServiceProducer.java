package io.fabric8.launcher.web.producers;

import java.util.stream.StreamSupport;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;

/**
 * Produces {@link GitService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitServiceProducer {

    private static final String GIT_PROVIDER_HEADER = "X-Git-Provider";

    @Inject
    private Instance<GitServiceFactory> gitServiceFactories;

    @Inject
    private GitHubServiceFactory defaultProvider;

    @Produces
    @RequestScoped
    GitService getGitService(HttpServletRequest request, IdentityProvider identityProvider) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        GitServiceFactory gitServiceFactory = getGitServiceFactory(request);
        Identity identity = gitServiceFactory.getDefaultIdentity()
                .orElseGet(
                        () -> identityProvider.getIdentity(gitServiceFactory.getName().toLowerCase(), authorization)
                                .orElseThrow(() -> new NotFoundException("Git token not found"))
                );
        return gitServiceFactory.create(identity);
    }

    private GitServiceFactory getGitServiceFactory(HttpServletRequest request) {
        GitServiceFactory result;
        String provider = request.getHeader(GIT_PROVIDER_HEADER);
        if (provider != null) {
            // Find provider by name
            result = StreamSupport.stream(gitServiceFactories.spliterator(), false)
                    .filter(g -> provider.equalsIgnoreCase(g.getName()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Git provider not found: " + provider));
        } else {
            result = defaultProvider;
        }
        return result;
    }

}