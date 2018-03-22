package io.fabric8.launcher.web.endpoints.websocket;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableMap;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.web.endpoints.HttpEndpoints;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Validation of the {@link MissionControlStatusEndpoint}
 */
@RunWith(Arquillian.class)
@RunAsClient
public class MissionControlStatusEndpointIT {

    static final String EXTRA_DATA_KEY = "GitHub project";

    @ArquillianResource
    private URI deploymentUri;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackages(true, StatusMessageEvent.class.getPackage())
                .addClass(HttpEndpoints.class)
                .addClass(TestEventEndpoint.class)
                .addClass(MissionControlStatusEndpoint.class);
    }

    /**
     * Ensures that CDI event is relayed over the webSocket status endpoint.
     *
     * @throws Exception when the test has failed
     */
    @Test
    public void webSocketsStatusTest() throws Exception {
        //given
        UUID uuid = UUID.randomUUID();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = UriBuilder.fromUri(deploymentUri).scheme("ws").path("status").path(uuid.toString()).build();
        final StatusTestClientEndpoint endpoint = new StatusTestClientEndpoint();
        container.connectToServer(endpoint, uri);

        //when
        sendMessage(uuid, "my first message");
        sendMessage(uuid, "my second message");
        endpoint.getLatch().await(3, TimeUnit.SECONDS);

        //then
        assertNotNull("a status message should have been send", endpoint.getMessage());
        assertTrue(endpoint.getMessage().contains(EXTRA_DATA_KEY));
        assertTrue(endpoint.getMessage().contains("my second message"));
    }

    private RequestSpecification configureTestEndpoint() {
        return new RequestSpecBuilder().setBaseUri(UriBuilder.fromUri(deploymentUri).path("api").path("test").build()).build();
    }

    private void sendMessage(final UUID uuid, final String message) {
        given().spec(configureTestEndpoint())
            .formParams(ImmutableMap.of("message", message))
            .when()
            .post("/event/" + uuid.toString())
            .then()
            .assertThat().statusCode(204);
    }

}
