package io.fabric8.launcher.core.impl.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class LocalStatusMessageEventBrokerTest {

    private LocalStatusMessageEventBroker broker;

    @Before
    public void setUp() {
        broker = new LocalStatusMessageEventBroker();
    }

    @Test
    public void should_bufferize_messages_without_consumer() throws Exception {
        //given
        UUID key = UUID.randomUUID();

        //when
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK));

        //then
        List<String> expected = Arrays.asList(
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE)),
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED)),
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK))
        );

        assertThat(broker.getBuffer().get(key)).hasSize(3);

        List<String> list = new ArrayList<>();
        broker.setConsumer(key, list::add);

        assertThat(list).containsExactlyElementsOf(expected);
        assertThat(broker.getBuffer().get(key)).isNullOrEmpty();
    }

    @Test
    public void should_send_to_consumer_without_buffering() throws Exception {
        //given
        UUID key = UUID.randomUUID();
        List<String> list = new ArrayList<>();

        //when
        broker.setConsumer(key, list::add);
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK));

        //then
        List<String> expected = Arrays.asList(
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE)),
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED)),
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK))
        );

        assertThat(list).containsExactlyElementsOf(expected);
        assertThat(broker.getBuffer().get(key)).isNullOrEmpty();
    }

    @Test
    public void should_not_trigger_consumer_after_removing() throws Exception {
        //given
        UUID key = UUID.randomUUID();
        List<String> list = new ArrayList<>();

        //when
        broker.setConsumer(key, list::add);
        broker.removeConsumer(key);

        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK));

        //then
        List<String> expected = Arrays.asList(
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE)),
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED)),
                JsonUtils.toString(new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK))
        );
        assertThat(list).isEmpty();
        assertThat(broker.getBuffer().get(key)).containsExactlyElementsOf(expected);
    }

    @After
    public void tearDown() {
        broker.close();
    }
}