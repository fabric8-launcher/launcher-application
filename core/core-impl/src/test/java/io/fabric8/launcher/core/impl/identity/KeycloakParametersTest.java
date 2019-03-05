package io.fabric8.launcher.core.impl.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class KeycloakParametersTest {

    @Test
    void testBuildUrl() {
        KeycloakParameters keycloakParameters = ImmutableKeycloakParameters.builder()
                .url("https://sso.prod-preview.openshift.io/auth")
                .realm("fabric8")
                .build();
        String url = keycloakParameters.buildTokenUrl("github");
        assertThat(url).isEqualTo("https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token");
    }
}