package io.fabric8.launcher.core.impl.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class LocalStatusMessageEventBrokerTest {

    LocalStatusMessageEventBroker broker;

    @BeforeEach
    public void createBroker() {
        broker = new LocalStatusMessageEventBroker();
    }

    @AfterEach
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

    private StatusMessageEvent githubWebhookEvent(UUID key) {
        return new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_WEBHOOK);
    }

    private StatusMessageEvent githubPushEvent(UUID key) {
        return new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_PUSHED);
    }

    private StatusMessageEvent githubCreateEvent(UUID key) {
        return new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_CREATE);
    }

}