package io.fabric8.launcher.core.impl.identity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
public final class LauncherIdentityProvider implements IdentityProvider {

    private final KeycloakService keycloakService;

    @Inject
    public LauncherIdentityProvider(final KeycloakService keycloakService) {
        this.keycloakService = requireNonNull(keycloakService, "keycloakService must be specified.");
    }

    @Override
    public CompletableFuture<Optional<Identity>> getIdentityAsync(final TokenIdentity authorization, final String service) {
        return keycloakService.getIdentity(authorization, service);
    }
}
