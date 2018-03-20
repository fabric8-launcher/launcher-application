package io.fabric8.launcher.core.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

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
@ApplicationScoped
public class DefaultIdentityProvider implements IdentityProvider {

    @Inject
    private Instance<KeycloakService> keycloakServiceInstance;

    @Override
    public Optional<Identity> getIdentity(String service, String authorization) {
        return keycloakServiceInstance.get().getIdentity(service, authorization);
    }
}
