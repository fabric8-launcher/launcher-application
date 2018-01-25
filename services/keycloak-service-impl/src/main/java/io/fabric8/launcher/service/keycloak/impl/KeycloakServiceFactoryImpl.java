package io.fabric8.launcher.service.keycloak.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.service.keycloak.api.KeycloakService;
import io.fabric8.launcher.service.keycloak.api.KeycloakServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class KeycloakServiceFactoryImpl implements KeycloakServiceFactory {

    private KeycloakServiceImpl keycloakService;

    @Override
    public KeycloakService create(String keyCloakURL, String realm) {
        return new KeycloakServiceImpl(keyCloakURL, realm);
    }

    @Override
    @Produces
    @ApplicationScoped
    public KeycloakService create() {
        if (keycloakService == null) {
            keycloakService = new KeycloakServiceImpl();
        }
        return keycloakService;
    }
}
