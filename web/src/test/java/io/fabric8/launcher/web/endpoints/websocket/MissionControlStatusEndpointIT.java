package io.fabric8.launcher.web.endpoints.websocket;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.fabric8.launcher.base.EnvironmentEnum;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.impl.CoreEnvVarSysPropNames;
import io.fabric8.launcher.core.impl.events.LocalStatusMessageEventBroker;
import io.fabric8.launcher.core.impl.producers.StatusMessageEventBrokerProducer;
import io.fabric8.launcher.web.endpoints.HttpEndpoints;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

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
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"annotated\" version=\"1.1\"/>"), "beans.xml")
                .addPackages(true, StatusMessageEvent.class.getPackage())
                .addClass(HttpEndpoints.class)
                .addClass(TestEventEndpoint.class)
                .addClass(MissionControlStatusEndpoint.class)
                .addClasses(EnvironmentSupport.class, EnvironmentEnum.class, CoreEnvVarSysPropNames.class)
                .addClasses(LocalStatusMessageEventBroker.class, StatusMessageEventBrokerProducer.class)
                .addClass(JsonUtils.class);
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
        OkHttpClient client = HttpClient.create().getClient();
        HttpUrl httpUrl = HttpUrl.get(deploymentUri).newBuilder("status/" + uuid).build();

        final StatusTestClientEndpoint endpoint = new StatusTestClientEndpoint();
        WebSocket webSocket = client.newWebSocket(new Request.Builder().url(httpUrl).build(), new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                endpoint.onOpen();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                endpoint.onMessage(text);
            }
        });

        //when
        sendMessage(uuid, "my first message");
        sendMessage(uuid, "my second message");
        endpoint.getLatch().await(3, TimeUnit.SECONDS);
        webSocket.close(1000, null);

        //then
        assertThat(endpoint.getMessages())
                .hasSize(2)
                .anyMatch(s -> s.contains("my first message"))
                .anyMatch(s -> s.contains("my second message"));
    }

    private RequestSpecification configureTestEndpoint() {
        return new RequestSpecBuilder().setBaseUri(deploymentUri + "api/test").build();
    }

    private void sendMessage(final UUID uuid, final String message) {
        given().spec(configureTestEndpoint())
                .formParams(singletonMap("message", message))
                .when()
                .post("/event/" + uuid.toString())
                .then()
                .assertThat().statusCode(204);
    }

}
