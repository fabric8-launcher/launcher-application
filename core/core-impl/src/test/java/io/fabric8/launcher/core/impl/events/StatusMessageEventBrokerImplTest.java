package io.fabric8.launcher.core.impl.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class StatusMessageEventBrokerImplTest {

    private StatusMessageEventBrokerImpl broker;

    @Before
    public void setUp() {
        broker = new StatusMessageEventBrokerImpl();
    }

    @Test
    public void should_bufferize_messages_without_consumer() {
        //given
        UUID key = UUID.randomUUID();

        //when
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK));

        //then
        assertThat(broker.getMessages().get(key)).hasSize(3);

        List<StatusMessageEvent> list = new ArrayList<>();
        broker.setConsumer(key, list::add);

        assertThat(list).hasSize(3).extracting(StatusMessageEvent::getStatusMessage)
                .containsSequence(StatusEventType.GITHUB_CREATE, StatusEventType.GITHUB_PUSHED, StatusEventType.GITHUB_WEBHOOK);
        assertThat(broker.getMessages().get(key)).isNullOrEmpty();
    }

    @Test
    public void should_send_to_consumer_without_buffering() {
        //given
        UUID key = UUID.randomUUID();
        List<StatusMessageEvent> list = new ArrayList<>();

        //when
        broker.setConsumer(key, list::add);
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK));

        //then
        assertThat(list).hasSize(3).extracting(StatusMessageEvent::getStatusMessage)
                .containsSequence(StatusEventType.GITHUB_CREATE, StatusEventType.GITHUB_PUSHED, StatusEventType.GITHUB_WEBHOOK);
        assertThat(broker.getMessages().get(key)).isNullOrEmpty();
    }

    @Test
    public void should_not_trigger_consumer_after_removing() {
        //given
        UUID key = UUID.randomUUID();
        List<StatusMessageEvent> list = new ArrayList<>();

        //when
        broker.setConsumer(key, list::add);
        broker.removeConsumer(key);

        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK));

        //then
        assertThat(list).isEmpty();
        assertThat(broker.getMessages().get(key)).hasSize(3).extracting(StatusMessageEvent::getStatusMessage)
                .containsSequence(StatusEventType.GITHUB_CREATE, StatusEventType.GITHUB_PUSHED, StatusEventType.GITHUB_WEBHOOK);

    }
}