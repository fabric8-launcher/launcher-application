package io.fabric8.launcher.osio.steps;

import io.fabric8.launcher.core.api.events.StatusEvent;

public enum OsioStatusEventType implements StatusEvent {

    CODEBASE_CREATED("Setting OSIO Codebase");

    OsioStatusEventType(String message) {
        this.message = message;
    }

    private final String message;

    @Override
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

}
