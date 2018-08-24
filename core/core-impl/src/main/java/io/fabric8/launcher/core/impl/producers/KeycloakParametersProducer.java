package io.fabric8.launcher.core.impl.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.core.impl.identity.ImmutableKeycloakParameters;
import io.fabric8.launcher.core.impl.identity.KeycloakParameters;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class KeycloakParametersProducer {

    @Produces
    @ApplicationScoped
    public KeycloakParameters produceKeycloakParameters() {
        return ImmutableKeycloakParameters.builder().build();
    }
}
