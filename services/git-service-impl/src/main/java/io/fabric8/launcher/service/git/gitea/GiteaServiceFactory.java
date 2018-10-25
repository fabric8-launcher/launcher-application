package io.fabric8.launcher.service.git.gitea;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment;
import io.fabric8.launcher.service.git.spi.GitProvider;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@GitProvider(GitProvider.GitProviderType.GITEA)
public class GiteaServiceFactory implements GitServiceFactory {

    private final HttpClient httpClient;

    @Inject
    public GiteaServiceFactory(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Gitea";
    }

    @Override
    public GiteaService create(Identity identity, String userName) {
        requireNonNull(identity, "Identity is required");
        requireNonNull(userName, "A logged user is required");
        return new GiteaService(identity, userName, httpClient);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        TokenIdentity identity = null;
        String token = GiteaEnvironment.LAUNCHER_BACKEND_GITEA_TOKEN.value();
        if (token != null) {
            identity = TokenIdentity.of(token);
        }
        return Optional.ofNullable(identity);
    }
}
