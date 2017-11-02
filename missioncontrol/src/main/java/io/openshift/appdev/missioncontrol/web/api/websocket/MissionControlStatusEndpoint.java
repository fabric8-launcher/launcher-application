package io.openshift.appdev.missioncontrol.web.api.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openshift.appdev.missioncontrol.core.api.StatusMessage;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;

/**
 * A websocket based resource that informs clients about the status of the operations
 *
 * https://abhirockzz.wordpress.com/2015/02/10/integrating-cdi-and-websockets/
 */
@ServerEndpoint(value = "/status/{uuid}")
public class MissionControlStatusEndpoint {
    private static final Logger log = Logger.getLogger(MissionControlStatusEndpoint.class.getName());

    private static final Map<UUID, Session> peers = new ConcurrentHashMap<>();

    private static final Map<UUID, List<String>> messageBuffer = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("uuid") String uuid) {
        UUID key = UUID.fromString(uuid);
        peers.put(key, session);
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (StatusMessage statusMessage : StatusMessage.values()) {
            JsonObjectBuilder object = Json.createObjectBuilder();
            builder.add(object.add(statusMessage.name(), statusMessage.getMessage()).build());
        }

        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
        asyncRemote.sendText(builder.build().toString());
        // Send pending messages
        List<String> messages = messageBuffer.remove(key);
        if (messages != null) {
            messages.forEach(asyncRemote::sendText);
        }
    }

    @OnClose
    public void onClose(@PathParam("uuid") String uuid) throws IOException {
        UUID key = UUID.fromString(uuid);
        peers.remove(key);
        messageBuffer.remove(key);
    }

    /**
     * Listen to status changes and pushes them to the registered sessions
     *
     * @param msg the status message to be send
     * @throws IOException when message could not be serialized to JSON
     */
    public void onEvent(@Observes StatusMessageEvent msg) throws IOException {
        UUID msgId = msg.getId();
        Session session = peers.get(msgId);
        String message = objectMapper.writeValueAsString(msg);
        if (session != null) {
            session.getAsyncRemote().sendText(message);
        } else {
            List<String> messages = messageBuffer.get(msgId);
            if (messages == null) {
                messages = new ArrayList<>();
                messageBuffer.put(msgId, messages);
            }
            messages.add(message);
            log.log(Level.WARNING, "No active WebSocket session was found for projectile {0}", msgId);
        }
    }
}