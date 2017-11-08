package io.openshift.appdev.missioncontrol.core.api;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Status message that wraps a {@link StatusMessage} and additional state
 */
public class StatusMessageEvent {
    private UUID id;
    private StatusMessage statusMessage;
    private Map<String, Object> data;

    public StatusMessageEvent(UUID uuid, Throwable e) {
        this(uuid, null, Collections.singletonMap("error", e.getMessage()));
    }

    public StatusMessageEvent(UUID uuid, StatusMessage statusMessage) {
        this(uuid, statusMessage, null);
    }

    public StatusMessageEvent(UUID uuid, StatusMessage statusMessage, Map<String, Object> data) {
        this.id = uuid;
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public UUID getId() {
        return id;
    }

    public StatusMessage getStatusMessage() {
        return statusMessage;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
