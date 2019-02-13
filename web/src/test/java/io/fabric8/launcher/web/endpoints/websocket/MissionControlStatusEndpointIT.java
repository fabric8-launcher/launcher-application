package io.fabric8.launcher.web.endpoints.websocket;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.fabric8.launcher.base.http.HttpClient;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation of the {@link MissionControlStatusEndpoint}
 */
@QuarkusTest
public class MissionControlStatusEndpointIT {

    static final String EXTRA_DATA_KEY = "GitHub project";

    @TestHTTPResource
    URI deploymentUri;

    /**
     * Ensures that CDI event is relayed over the webSocket status endpoint.
     *
     * @throws Exception when the test has failed
     */
    @Test
    void webSocketsStatusTest() throws Exception {
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


    private void sendMessage(final UUID uuid, final String message) {
        given()
                .formParams(singletonMap("message", message))
                .when()
                .post("/api/test/event/" + uuid.toString())
                .then()
                .assertThat().statusCode(204);
    }

}
