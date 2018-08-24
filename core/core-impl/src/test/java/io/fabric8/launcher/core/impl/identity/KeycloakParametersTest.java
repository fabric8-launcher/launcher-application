package io.fabric8.launcher.core.impl.identity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class KeycloakParametersTest {

    @Test
    public void testBuildUrl() {
        KeycloakParameters keycloakParameters = ImmutableKeycloakParameters.builder()
                .url("https://sso.prod-preview.openshift.io/auth")
                .realm("fabric8")
                .build();
        String url = keycloakParameters.buildUrl("github");
        Assert.assertEquals("https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }

}
