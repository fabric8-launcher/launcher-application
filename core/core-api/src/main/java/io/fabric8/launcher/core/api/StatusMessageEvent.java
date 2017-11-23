package io.fabric8.launcher.core.api;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Status message that wraps a {@link StatusEventType} and additional state
 */
public class StatusMessageEvent {
    public StatusMessageEvent(UUID uuid, Throwable e) {
        this(uuid, null, Collections.singletonMap("error", e.getMessage()));
    }

    public StatusMessageEvent(UUID uuid, StatusEventType statusEventType) {
        this(uuid, statusEventType, null);
    }

    public StatusMessageEvent(UUID uuid, StatusEventType statusEventType, Map<String, Object> data) {
        this.id = uuid;
        this.statusEventType = statusEventType;
        this.data = data;
    }

    private UUID id;

    private StatusEventType statusEventType;

    private Map<String, Object> data;

    public UUID getId() {
        return id;
    }

    public StatusEventType getStatusMessage() {
        return statusEventType;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
