package io.fabric8.launcher.web.endpoints.osio;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

import io.fabric8.launcher.core.api.events.StatusEventKind;

import static io.fabric8.launcher.core.api.events.LauncherStatusEventKind.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.LauncherStatusEventKind.OPENSHIFT_PIPELINE;
import static io.fabric8.launcher.osio.steps.OsioStatusEventKind.CODEBASE_CREATED;

@ClientEndpoint
public class OsioStatusClientEndpoint {

    private static final Logger LOG = Logger.getLogger(OsioStatusClientEndpoint.class.getName());

    private final CountDownLatch latch = new CountDownLatch(2);
    private boolean githubPushed = false;

    private boolean codebaseCreated;

    public boolean isGithubPushed(){
        return githubPushed;
    }

    public boolean isCodebaseCreated() {
        return codebaseCreated;
    }

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
        if (receivedEventIs(message, OPENSHIFT_PIPELINE)){
            latch.countDown();
        } else if (receivedEventIs(message, GITHUB_PUSHED)) {
            this.githubPushed = true;
        } else if (receivedEventIs(message, CODEBASE_CREATED)) {
            this.codebaseCreated = true;
        }
    }

    private boolean receivedEventIs(String message, StatusEventKind event) {
        return message != null && message.contains("\"statusMessage\":\"" + event.name()  + "\"");
    }
}
