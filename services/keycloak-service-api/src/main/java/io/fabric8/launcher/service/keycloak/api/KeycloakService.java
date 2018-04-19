package io.fabric8.launcher.service.keycloak.api;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;

/**
 * API on top of Keycloak
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface KeycloakService {

    /**
     * Grabs the {@link Identity} for the specified provider
     *
     * @param authorization the {@link TokenIdentity} authorization
     * @param provider The identity provider to use
     * @return an {@link Optional} containing an {@link Identity}
     */
    CompletableFuture<Optional<Identity>> getIdentity(TokenIdentity authorization, String provider);

}
