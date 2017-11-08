package io.openshift.appdev.missioncontrol.service.keycloak.impl;

import io.openshift.appdev.missioncontrol.service.keycloak.impl.KeycloakServiceImpl;
import org.junit.Assert;
import org.junit.Test;

public class KeycloakServiceTest {
    @Test
    public void testBuildUrl() {
        String url = KeycloakServiceImpl.buildURL("https://sso.prod-preview.openshift.io/auth", "fabric8", "github");
        Assert.assertEquals("https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRequestURL() {
        KeycloakServiceImpl service = new KeycloakServiceImpl("foo","realm");
        service.getOpenShiftIdentity("anything");
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidRequest() {
        //Service should not be available
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://localhost:5555","realm");
        service.getOpenShiftIdentity("token");
        Assert.fail("Should have thrown IllegalStateException");
    }
}
