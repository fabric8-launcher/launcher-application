package io.fabric8.launcher.web.endpoints.osio;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

@ClientEndpoint
public class OsioStatusClientEndpoint {

    private static final Logger LOG = Logger.getLogger(OsioStatusClientEndpoint.class.getName());

    private final CountDownLatch latch = new CountDownLatch(2);

    public CountDownLatch getLatch() {
        return latch;
    }

    @OnOpen
    public void onOpen() {
        LOG.info("WS Session open");
    }

    @OnMessage
    public void onMessage(String message) {
        LOG.info("Event received: " + message);
        if(message != null && message.contains("\"statusMessage\":\"OPENSHIFT_PIPELINE\"")){
            latch.countDown();
        }
    }

}
