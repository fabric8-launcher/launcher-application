package io.fabric8.launcher.web.endpoints.websocket;

import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

/**
 * Websocket message listener for assertions.
 */
@ClientEndpoint
public class StatusTestClientEndpoint {
    private CountDownLatch latch = new CountDownLatch(2);

    private String message;

    @OnOpen
    public void onOpen() {
        System.out.println("WS Session open");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("################### MESSAGE: "+message);
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
