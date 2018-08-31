package io.fabric8.launcher.web.endpoints.websocket;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;

/**
 * A websocket based resource that informs clients about the status of the operations
 *
 * Based on https://abhirockzz.wordpress.com/2015/02/10/integrating-cdi-and-websockets/
 */
@Dependent
@ServerEndpoint(value = "/status/{uuid}")
public class MissionControlStatusEndpoint {
    private static final Logger logger = Logger.getLogger(MissionControlStatusEndpoint.class.getName());

    @Inject
    private StatusMessageEventBroker statusMessageEventBroker;

    @OnOpen
    public void onOpen(Session session, @PathParam("uuid") String uuid) {
        logger.log(Level.INFO, "WebSocket session opened: {0}", uuid);
        UUID key = UUID.fromString(uuid);
        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
        statusMessageEventBroker.setConsumer(key, asyncRemote::sendText);
    }

    @OnClose
    public void onClose(@PathParam("uuid") String uuid) {
        logger.log(Level.INFO, "WebSocket session closed: {0}", uuid);
        UUID key = UUID.fromString(uuid);
        statusMessageEventBroker.removeConsumer(key);
    }
}