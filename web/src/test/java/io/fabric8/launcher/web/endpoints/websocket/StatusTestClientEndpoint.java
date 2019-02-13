package io.fabric8.launcher.web.endpoints.websocket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

/**
 * Websocket message listener for assertions.
 */
class StatusTestClientEndpoint {
    private CountDownLatch latch = new CountDownLatch(2);

    private List<String> messages = new CopyOnWriteArrayList<>();

    void onOpen() {
        System.out.println("WS Session open");
    }

    void onMessage(String message) {
        System.out.println("################### MESSAGE: "+message);
        this.messages.add(message);
        latch.countDown();
    }

    List<String> getMessages() {
        return messages;
    }

    CountDownLatch getLatch() {
        return latch;
    }
}
