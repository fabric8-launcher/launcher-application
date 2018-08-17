package io.fabric8.launcher.osio.steps;

import io.fabric8.launcher.core.api.events.StatusEventKind;

/**
 * OSIO-specific status messages used in web sockets to inform clients
 * about the progress of the background operations
 */
public enum OsioStatusEventKind implements StatusEventKind {

    CODEBASE_CREATED("Setting OSIO Codebase");

    OsioStatusEventKind(String message) {
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
