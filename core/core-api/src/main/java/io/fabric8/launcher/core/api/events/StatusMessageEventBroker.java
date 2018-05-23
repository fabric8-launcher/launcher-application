package io.fabric8.launcher.core.api.events;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A {@link StatusMessageEventBroker} abstracts the handling of {@link StatusMessageEvent} objects sent during the course of launching.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface StatusMessageEventBroker extends AutoCloseable {

    /**
     * Registers a {@link Consumer} for {@link StatusMessageEvent} objects using the given key.
     * Only one consumer is supported at the moment.
     *
     * @param key      a key used to listen for messages
     * @param consumer the {@link Consumer} to be called when a message is available
     */
    void setConsumer(UUID key, Consumer<StatusMessageEvent> consumer);

    /**
     * Removes any {@link Consumer} registered for this key
     *
     * @param key a key used to listen for messages
     */
    void removeConsumer(UUID key);

    /**
     * Sends the event to the buffer
     *
     * @param event
     */
    void send(StatusMessageEvent event);

    /**
     * Performs internal cleanup
     */
    @Override
    void close();
}
