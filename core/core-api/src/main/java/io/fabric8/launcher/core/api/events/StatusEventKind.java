package io.fabric8.launcher.core.api.events;


/**
 * Contract for all status messages that we send to the clients via
 * websockets to inform them about the status of their project.
 */
public interface StatusEventKind {

    String name();

    String message();

    String toString();
}
