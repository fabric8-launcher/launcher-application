package io.fabric8.launcher.core.impl.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import org.jetbrains.annotations.NotNull;
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
    public void createBroker() {
        broker = new LocalStatusMessageEventBroker();
    }

    @After
    public void closeBroker() {
        broker.close();
    }

    @Test
    public void should_bufferize_messages_without_consumer() throws Exception {
        //given
        UUID key = UUID.randomUUID();
        List<String> expectedEvents = Arrays.asList(
                asJson(githubCreateEvent(key)),
                asJson(githubPushEvent(key)),
                asJson(githubWebhookEvent(key))
        );

        //when
        broker.send(githubCreateEvent(key));
        broker.send(githubPushEvent(key));
        broker.send(githubWebhookEvent(key));

        //then
        assertThat(broker.getBuffer().get(key)).hasSize(3);

        List<String> consumedEvents = new ArrayList<>();
        broker.setConsumer(key, consumedEvents::add);

        assertThat(consumedEvents).containsExactlyElementsOf(expectedEvents);
        assertThat(broker.getBuffer().get(key)).isNullOrEmpty();
    }

    @Test
    public void should_send_to_consumer_without_buffering() throws Exception {
        //given
        UUID key = UUID.randomUUID();
        List<String> consumedEvents = new ArrayList<>();
        List<String> expectedEvents = Arrays.asList(
                asJson(githubCreateEvent(key)),
                asJson(githubPushEvent(key)),
                asJson(githubWebhookEvent(key))
        );

        //when
        broker.setConsumer(key, consumedEvents::add);
        broker.send(githubCreateEvent(key));
        broker.send(githubPushEvent(key));
        broker.send(githubWebhookEvent(key));

        //then
        assertThat(consumedEvents).containsExactlyElementsOf(expectedEvents);
        assertThat(broker.getBuffer().get(key)).isNullOrEmpty();
    }

    @Test
    public void should_not_trigger_consumer_after_removing() throws Exception {
        //given
        UUID key = UUID.randomUUID();
        List<String> consumedEvents = new ArrayList<>();
        List<String> expectedEvents = Arrays.asList(
                asJson(githubCreateEvent(key)),
                asJson(githubPushEvent(key)),
                asJson(githubWebhookEvent(key))
        );

        //when
        broker.setConsumer(key, consumedEvents::add);
        broker.removeConsumer(key);

        broker.send(githubCreateEvent(key));
        broker.send(githubPushEvent(key));
        broker.send(githubWebhookEvent(key));

        //then
        assertThat(consumedEvents).isEmpty();
        assertThat(broker.getBuffer().get(key)).containsExactlyElementsOf(expectedEvents);
    }

    private String asJson(StatusMessageEvent event) throws IOException {
        return JsonUtils.toString(event);
    }

    @NotNull
    private StatusMessageEvent githubWebhookEvent(UUID key) {
        return new StatusMessageEvent(key, StatusEventType.GITHUB_WEBHOOK);
    }

    @NotNull
    private StatusMessageEvent githubPushEvent(UUID key) {
        return new StatusMessageEvent(key, StatusEventType.GITHUB_PUSHED);
    }

    @NotNull
    private StatusMessageEvent githubCreateEvent(UUID key) {
        return new StatusMessageEvent(key, StatusEventType.GITHUB_CREATE);
    }

}