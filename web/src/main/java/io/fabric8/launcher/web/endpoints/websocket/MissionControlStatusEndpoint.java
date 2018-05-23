package io.fabric8.launcher.web.endpoints.websocket;

import java.util.UUID;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;

/**
 * A websocket based resource that informs clients about the status of the operations
 *
 * https://abhirockzz.wordpress.com/2015/02/10/integrating-cdi-and-websockets/
 */
@Dependent
@ServerEndpoint(value = "/status/{uuid}")
public class MissionControlStatusEndpoint {
    private static final Logger logger = Logger.getLogger(MissionControlStatusEndpoint.class.getName());

    @Inject
    private StatusMessageEventBroker statusMessageEventBroker;

    @OnOpen
    public void onOpen(Session session, @PathParam("uuid") String uuid) {
        logger.info("WebSocket session opened using UUID: " + uuid);
        UUID key = UUID.fromString(uuid);
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (StatusEventType statusEventType : StatusEventType.values()) {
            JsonObjectBuilder object = Json.createObjectBuilder();
            builder.add(object.add(statusEventType.name(), statusEventType.getMessage()).build());
        }

        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
        asyncRemote.sendText(builder.build().toString());

        statusMessageEventBroker.setConsumer(key, asyncRemote::sendText);
    }

    @OnClose
    public void onClose(@PathParam("uuid") String uuid) {
        logger.info("WebSocket session closed using UUID: " + uuid);
        UUID key = UUID.fromString(uuid);
        statusMessageEventBroker.removeConsumer(key);
    }
}