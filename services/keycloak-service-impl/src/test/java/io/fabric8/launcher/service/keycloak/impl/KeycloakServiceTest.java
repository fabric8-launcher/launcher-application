package io.fabric8.launcher.service.keycloak.impl;

import org.junit.Assert;
import org.junit.Test;

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
        assertThatThrownBy(() -> service.getOpenShiftIdentity("anything")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unexpected url");
    }

    @Test
    public void testInvalidRequest() {
        //Service should not be available
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://localhost:5555", "realm");
        assertThatThrownBy(() -> service.getOpenShiftIdentity("token")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Error while fetching token");
    }
}
