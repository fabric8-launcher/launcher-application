package io.fabric8.launcher.core.impl.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;

import static java.util.Objects.requireNonNull;

/**
 * The default implementation for {@link StatusMessageEventBroker}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class LocalStatusMessageEventBroker implements StatusMessageEventBroker {

    private Map<UUID, List<String>> buffer = new ConcurrentHashMap<>();

    private Map<UUID, Consumer<String>> consumers = new ConcurrentHashMap<>();

    @Override
    public void setConsumer(UUID key, Consumer<String> consumer) {
        requireNonNull(key, "Key must be specified");
        requireNonNull(consumer, "Consumer must be specified");
        consumers.put(key, consumer);
        // Flush cached buffer
        List<String> events = buffer.remove(key);
        if (events != null) {
            events.forEach(consumer);
        }
    }

    @Override
    public void removeConsumer(UUID key) {
        requireNonNull(key, "Key must be specified");
        buffer.remove(key);
        consumers.remove(key);
    }

    @Override
    public void send(StatusMessageEvent event) {
        requireNonNull(event, "Event must be specified");
        String message;
        try {
            message = JsonUtils.toString(event);
        } catch (IOException e) {
            // Should never happen
            throw new UncheckedIOException(e);
        }
        Consumer<String> consumer = consumers.get(event.getId());
        if (consumer == null) {
            // No consumer found, store buffer in a temporary cache
            buffer.computeIfAbsent(event.getId(), k -> new ArrayList<>()).add(message);
        } else {
            // No need to cache, just delegate to consumer
            consumer.accept(message);
        }
    }

    @Override
    public void close() {
        buffer.clear();
        consumers.clear();
    }

    /**
     * For testing purposes only
     */
    Map<UUID, List<String>> getBuffer() {
        return buffer;
    }
}
