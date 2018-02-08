package io.fabric8.launcher.core.spi;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;

/**
 * String gitHubToken = new KeycloakClient().getTokenFor(KeycloakEndpoint.GET_GITHUB_TOKEN, authHeader);
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IdentityProvider {

    Optional<Identity> getIdentity(String service, String authorization);

    interface ServiceType {
        String GITHUB = "github";
        String OPENSHIFT = "openshift-v3";
    }

}
