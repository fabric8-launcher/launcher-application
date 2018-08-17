package io.fabric8.launcher.core.api.events;

public interface StatusEvent {

    String name();

    String message();

    String toString();
}
