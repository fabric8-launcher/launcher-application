package io.fabric8.launcher.core.impl.identity;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;
import static java.util.Objects.requireNonNull;

/**
 * fabric8-launcher requires a Keycloak configured with the "rh-developers-launch" realm
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(LAUNCHER)
@Dependent
public class LauncherIdentityProvider implements IdentityProvider {

    private final TokenIdentity authorization;
    private final KeycloakService keycloakService;

    @Inject
    public LauncherIdentityProvider(final TokenIdentity authorization, final KeycloakService keycloakService) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
        this.keycloakService = requireNonNull(keycloakService, "keycloakService must be specified.");
    }

    @Override
    public Optional<Identity> getIdentity(String service) {
        return keycloakService.getIdentity(service, authorization.toRequestHeaderValue());
    }
}
