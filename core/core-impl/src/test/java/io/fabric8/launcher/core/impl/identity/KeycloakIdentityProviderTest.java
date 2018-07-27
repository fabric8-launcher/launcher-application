package io.fabric8.launcher.core.impl.identity;

import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KeycloakIdentityProviderTest {
    @Test
    public void testBuildUrl() {
        String url = KeycloakIdentityProvider.buildURL("https://sso.prod-preview.openshift.io/auth", "fabric8", "github");
        Assert.assertEquals("https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }

    @Test
    public void testInvalidRequestURL() {
        IdentityProvider identityProvider = new KeycloakIdentityProvider("foo", "realm");
        assertThatThrownBy(() -> identityProvider.getIdentityAsync(TokenIdentity.of("anything"), "openshift-v3").get())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInvalidRequest() throws ExecutionException, InterruptedException {
        //Service should not be available
        IdentityProvider identityProvider = new KeycloakIdentityProvider("http://localhost:5555", "realm");
        assertThat(identityProvider.getIdentityAsync(TokenIdentity.of("token"), "openshift-v3").get())
                .isEmpty();
    }
}
