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
     * @param authorization authorization header
     * @param cluster the cluster to use to get the token for
     * @return the openshift v3 token assigned to the given keycloak access token
     */
    Identity getOpenShiftIdentity(String authorization, String cluster);

    /**
     * Returns the GitHub {@link Identity} used for authentication with Github
     *
     * @param token the keycloak access token
     * @return the github Identity token assigned to the given keycloak access token
     */
    Identity getGitHubIdentity(String token);

    /**
     * Grabs the {@link Identity} for the specified provider
     *
     * @param provider The identity provider to use
     * @param token    the keycloak access token
     * @return an {@link Optional} containing an {@link Identity}
     */
    Optional<Identity> getIdentity(String provider, String token);
}
