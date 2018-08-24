package io.fabric8.launcher.core.api.events;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Status message that wraps a {@link LauncherStatusEventKind} and additional state
 */
public class StatusMessageEvent {
    public StatusMessageEvent(UUID uuid, Throwable e) {
        this(uuid, null, Collections.singletonMap("error", e.getMessage()));
    }

    public StatusMessageEvent(UUID uuid, StatusEventKind kind) {
        this(uuid, kind, null);
    }

    public StatusMessageEvent(UUID uuid, StatusEventKind kind, Map<String, Object> data) {
        this.id = uuid;
        this.kind = kind;
        this.data = data;
    }

    private UUID id;

    private StatusEventKind kind;

    private Map<String, Object> data;

    public UUID getId() {
        return id;
    }

    public StatusEventKind getStatusMessage() {
        return kind;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
