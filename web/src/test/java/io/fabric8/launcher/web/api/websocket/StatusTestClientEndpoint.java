package io.fabric8.launcher.web.api.websocket;

import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;

/**
 * Websocket message listener for assertions.
 */
@ClientEndpoint
@ApplicationScoped
public class StatusTestClientEndpoint {
    private CountDownLatch latch = new CountDownLatch(2);

    private String message;

    @OnMessage
    public void onMessage(String message) {
        this.message = message;
        latch.countDown();
    }

    public String getMessage() {
        return message;
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
