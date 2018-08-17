package io.fabric8.launcher.core.api.events;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Status message that wraps a {@link LauncherStatusEventType} and additional state
 */
public class StatusMessageEvent {
    public StatusMessageEvent(UUID uuid, Throwable e) {
        this(uuid, null, Collections.singletonMap("error", e.getMessage()));
    }

    public StatusMessageEvent(UUID uuid, StatusEvent statusEvent) {
        this(uuid, statusEvent, null);
    }

    public StatusMessageEvent(UUID uuid, StatusEvent statusEvent, Map<String, Object> data) {
        this.id = uuid;
        this.statusEvent = statusEvent;
        this.data = data;
    }

    private UUID id;

    private StatusEvent statusEvent;

    private Map<String, Object> data;

    public UUID getId() {
        return id;
    }

    public StatusEvent getStatusMessage() {
        return statusEvent;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
