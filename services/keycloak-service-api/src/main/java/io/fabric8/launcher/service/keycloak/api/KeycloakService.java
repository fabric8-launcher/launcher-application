package io.fabric8.launcher.service.keycloak.api;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;

/**
 * API on top of Keycloak
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface KeycloakService {

    /**
     * Returns the Openshift v3 {@link Identity} used for authenticating with the Openshift console
     *
     * @param authorization the {@link TokenIdentity} authorization
     * @return the openshift v3 token assigned to the given keycloak access token
     */
    Identity getOpenShiftIdentity(TokenIdentity authorization);

    /**
     * Returns the GitHub {@link Identity} used for authentication with Github
     *
     * @param authorization the {@link TokenIdentity} authorization
     * @return the github Identity token assigned to the given keycloak access token
     */
    Identity getGitHubIdentity(TokenIdentity authorization);

    /**
     * Grabs the {@link Identity} for the specified provider
     *
     * @param authorization the {@link TokenIdentity} authorization
     * @param provider The identity provider to use
     * @return an {@link Optional} containing an {@link Identity}
     */
    Optional<Identity> getIdentity(TokenIdentity authorization, String provider);
}
