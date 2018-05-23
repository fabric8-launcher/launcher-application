package io.fabric8.launcher.core.impl.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;

import static java.util.Objects.requireNonNull;

/**
 * The default implementation for {@link StatusMessageEventBroker}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class StatusMessageEventBrokerImpl implements StatusMessageEventBroker {

    private Map<UUID, List<StatusMessageEvent>> messages = new ConcurrentHashMap<>();

    private Map<UUID, Consumer<StatusMessageEvent>> consumers = new ConcurrentHashMap<>();

    @Override
    public void setConsumer(UUID key, Consumer<StatusMessageEvent> consumer) {
        requireNonNull(key, "Key must be specified");
        requireNonNull(consumer, "Consumer must be specified");
        consumers.put(key, consumer);
        // Flush cached messages
        List<StatusMessageEvent> events = messages.remove(key);
        if (events != null) {
            events.forEach(consumer);
        }
    }

    @Override
    public void removeConsumer(UUID key) {
        requireNonNull(key, "Key must be specified");
        messages.remove(key);
        consumers.remove(key);
    }

    @Override
    public void send(StatusMessageEvent event) {
        requireNonNull(event, "Event must be specified");
        Consumer<StatusMessageEvent> consumer = consumers.get(event.getId());
        if (consumer == null) {
            // No consumer found, store messages in a temporary cache
            messages.computeIfAbsent(event.getId(), k -> new ArrayList<>()).add(event);
        } else {
            // No need to cache, just delegate to consumer
            consumer.accept(event);
        }
    }

    /**
     * For testing purposes only
     */
    Map<UUID, List<StatusMessageEvent>> getMessages() {
        return messages;
    }

    Map<UUID, Consumer<StatusMessageEvent>> getConsumers() {
        return consumers;
    }
}
