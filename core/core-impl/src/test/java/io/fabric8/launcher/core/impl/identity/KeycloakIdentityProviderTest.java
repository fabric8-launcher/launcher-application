package io.fabric8.launcher.core.impl.identity;

import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KeycloakIdentityProviderTest {

    @Test
    public void testInvalidRequestURL() {
        IdentityProvider identityProvider = new KeycloakIdentityProvider(ImmutableKeycloakParameters.builder().url("foo").realm("realm").build(), HttpClient.create());
        assertThatThrownBy(() -> identityProvider.getIdentityAsync(TokenIdentity.of("anything"), "openshift-v3").get())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInvalidRequest() throws ExecutionException, InterruptedException {
        //Service should not be available
        IdentityProvider identityProvider = new KeycloakIdentityProvider(ImmutableKeycloakParameters.builder().url("http://localhost:5555").realm("realm").build(), HttpClient.create());
        assertThat(identityProvider.getIdentityAsync(TokenIdentity.of("token"), "openshift-v3").get())
                .isEmpty();
    }
}
