package io.fabric8.launcher.service.git.gitea;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment;
import io.fabric8.launcher.service.git.spi.GitProvider;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
@GitProvider(GitProvider.GitProviderType.GITEA)
public class GiteaServiceFactory implements GitServiceFactory {

    private final HttpServletRequest request;

    private final HttpClient httpClient;

    @Inject
    public GiteaServiceFactory(HttpServletRequest request, HttpClient httpClient) {
        //TODO: Verify if HttpServletRequest can be replaced with the user name instead
        this.request = request;
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Gitea";
    }


    @Override
    public GiteaService create() {
        return create(getDefaultIdentity().orElseThrow(() -> new IllegalStateException("Cannot find the default identity needed in " + getClass().getName() + ".create()")));
    }

    @Override
    public GiteaService create(Identity identity) {
        String userName = (String) request.getAttribute("USER_NAME");
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
