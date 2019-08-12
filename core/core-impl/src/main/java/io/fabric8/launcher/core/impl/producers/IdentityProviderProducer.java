package io.fabric8.launcher.core.impl.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.core.impl.identity.ImmutableKeycloakParameters;
import io.fabric8.launcher.core.impl.identity.KeycloakIdentityProvider;
import io.fabric8.launcher.core.spi.IdentityProvider;

import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_KEYCLOAK_REALM;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_KEYCLOAK_URL;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class IdentityProviderProducer {

    @Produces
    @ApplicationScoped
    IdentityProvider produceIdentityProvider(HttpClient httpClient) {
        if (LAUNCHER_KEYCLOAK_URL.isSet() && LAUNCHER_KEYCLOAK_REALM.isSet()) {
            return new KeycloakIdentityProvider(ImmutableKeycloakParameters.builder().build(), httpClient);
        }
        return IdentityProvider.NULL_PROVIDER;
    }
}