package io.fabric8.launcher.osio.providers;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.http.ExternalRequest;
import okhttp3.Request;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.TokenIdentity.removeBearerPrefix;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(OSIO)
@RequestScoped
public class OsioIdentityProvider implements IdentityProvider {

    @Override
    public Optional<Identity> getIdentity(String service, String authorization) {
        switch (service) {
            case ServiceType.GITHUB:
                Request gitHubTokenRequest = new Request.Builder()
                        .url(EnvironmentVariables.ExternalServices.getGithubTokenURL())
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .build();
                return ExternalRequest.readJson(gitHubTokenRequest, tree -> tree.get("access_token").asText())
                        .map(IdentityFactory::createFromToken);
            case ServiceType.OPENSHIFT:
                return Optional.of(createFromToken(removeBearerPrefix(authorization)));
            default:
                return Optional.empty();
        }
    }
}
