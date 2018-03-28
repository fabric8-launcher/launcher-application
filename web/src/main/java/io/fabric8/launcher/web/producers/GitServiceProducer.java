package io.fabric8.launcher.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProvider;
import io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType;

import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.GITHUB;
import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.valueOf;

/**
 * Produces {@link GitService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class GitServiceProducer {

    private static final String GIT_PROVIDER_HEADER = "X-Git-Provider";

    @Inject
    @Any
    private Instance<GitServiceFactory> gitServiceFactories;

    @Inject
    @GitProvider(GITHUB)
    private GitServiceFactory defaultProvider;

    @Produces
    @RequestScoped
    GitService getGitService(HttpServletRequest request, IdentityProvider identityProvider) {
        GitServiceFactory gitServiceFactory = getGitServiceFactory(request);
        Identity identity = gitServiceFactory.getDefaultIdentity()
                .orElseGet(
                        () -> identityProvider.getIdentity(gitServiceFactory.getName().toLowerCase())
                                .orElseThrow(() -> new NotFoundException("Git token not found"))
                );
        return gitServiceFactory.create(identity);
    }

    private GitServiceFactory getGitServiceFactory(HttpServletRequest request) {
        GitServiceFactory result;
        String provider = request.getHeader(GIT_PROVIDER_HEADER);
        if (provider != null) {
            GitProviderType type = valueOf(provider.toUpperCase());
            result = gitServiceFactories.select(GitProvider.GitProviderLiteral.of(type)).get();
        } else {
            result = defaultProvider;
        }
        return result;
    }

}