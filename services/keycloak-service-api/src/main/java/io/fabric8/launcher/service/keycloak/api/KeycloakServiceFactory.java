package io.fabric8.launcher.service.keycloak.api;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface KeycloakServiceFactory {

    /**
     * Returns a {@link KeycloakService} for the given URL and realm
     *
     * @param keyCloakURL the keycloak URL
     * @param realm       the realm to use
     * @return
     */
    KeycloakService create(String keyCloakURL, String realm);

    /**
     * @return the default Keycloak instance. May throw an {@link IllegalStateException} if not defined
     */
    KeycloakService create();
}
