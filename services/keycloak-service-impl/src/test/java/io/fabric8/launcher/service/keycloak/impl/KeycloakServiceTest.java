package io.fabric8.launcher.service.keycloak.impl;

import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.base.identity.TokenIdentity;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KeycloakServiceTest {
    @Test
    public void testBuildUrl() {
        String url = KeycloakServiceImpl.buildURL("https://sso.prod-preview.openshift.io/auth", "fabric8", "github");
        Assert.assertEquals("https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }

    @Test
    public void testInvalidRequestURL() {
        KeycloakServiceImpl service = new KeycloakServiceImpl("foo", "realm");
        assertThatThrownBy(() -> service.getIdentity(TokenIdentity.of("anything"), "openshift-v3").get()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unexpected url");
    }

    @Test
    public void testInvalidRequest() throws ExecutionException, InterruptedException {
        //Service should not be available
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://localhost:5555", "realm");
        assertThat(service.getIdentity(TokenIdentity.of("token"), "openshift-v3").get())
                .isEmpty();
    }
}
