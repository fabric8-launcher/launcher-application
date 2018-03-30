package io.fabric8.launcher.service.keycloak.api;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;

/**
 * API on top of Keycloak
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface KeycloakService {

    /**
     * Returns the Openshift v3 {@link Identity} used for authenticating with the Openshift console
     *
     * @param authorizationHeader the authorization header
     * @return the openshift v3 token assigned to the given keycloak access token
     */
    Identity getOpenShiftIdentity(String authorizationHeader);

    /**
     * Returns the GitHub {@link Identity} used for authentication with Github
     *
     * @param authorizationHeader the authorization header
     * @return the github Identity token assigned to the given keycloak access token
     */
    Identity getGitHubIdentity(String authorizationHeader);

    /**
     * Grabs the {@link Identity} for the specified provider
     *
     * @param provider The identity provider to use
     * @param authorizationHeader  the authorization header
     * @return an {@link Optional} containing an {@link Identity}
     */
    Optional<Identity> getIdentity(String provider, String authorizationHeader);
}
