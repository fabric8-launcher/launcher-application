package io.fabric8.launcher.core.impl;

import java.util.Optional;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;

/**
 * fabric8-launcher requires a Keycloak configured with the "rh-developers-launch" realm
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Named("fabric8-launcher")
@Default
public class Fabric8LauncherIdentityProvider implements IdentityProvider {

    @Inject
    private Instance<KeycloakService> keycloakServiceInstance;

    @Override
    public Optional<Identity> getIdentity(String service, String authorization) {
        return keycloakServiceInstance.get().getIdentity(service, authorization);
    }
}
