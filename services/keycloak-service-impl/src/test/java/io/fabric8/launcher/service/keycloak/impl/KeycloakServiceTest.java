package io.fabric8.launcher.service.keycloak.impl;

import java.io.IOException;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class KeycloakServiceTest {
    @Test
    public void testBuildUrl() {
        String url = KeycloakServiceImpl.buildURL("https://sso.prod-preview.openshift.io/auth", "fabric8", "github");
        assertEquals("https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRequestURL() {
        KeycloakServiceImpl service = new KeycloakServiceImpl("foo", "realm");
        service.getOpenShiftIdentity("anything", "cluster");
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidRequest() {
        //Service should not be available
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://localhost:5555", "realm");
        service.getOpenShiftIdentity("token", null);
        fail("Should have thrown IllegalStateException");
    }


    @Test
    public void testValidRequest() throws InterruptedException, IOException {
        // given
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("{\"access_token\": \"theToken\"}"));

        KeycloakServiceImpl service = new KeycloakServiceImpl(server.url("/endpoint").url().toString(), "realm");

        // when
        Identity identity = service.getOpenShiftIdentity("token", "cluster-name");

        // then
        assertNotNull(identity);
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                assertNotNull(token);
                assertEquals("theToken", token.getToken());
            }
        });

        server.shutdown();
        assertEquals("GET /endpoint/realms/realm/broker/cluster-name/token HTTP/1.1",
                            server.takeRequest().getRequestLine());
    }
}
