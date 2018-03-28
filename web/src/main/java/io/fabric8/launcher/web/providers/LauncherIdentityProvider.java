package io.fabric8.launcher.web.providers;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;

/**
 * fabric8-launcher requires a Keycloak configured with the "rh-developers-launch" realm
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(LAUNCHER)
@RequestScoped
public class LauncherIdentityProvider implements IdentityProvider {

    @Inject
    private Instance<KeycloakService> keycloakServiceInstance;

    @Inject
    private HttpServletRequest request;

    @Override
    public Optional<Identity> getIdentity(String service) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return keycloakServiceInstance.get().getIdentity(service, authorization);
    }
}
