package io.fabric8.launcher.core.impl.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import org.apache.activemq.artemis.junit.EmbeddedJMSResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ArtemisStatusMessageEventBrokerTest {
    @Rule
    public EmbeddedJMSResource resource = new EmbeddedJMSResource();

    private ArtemisStatusMessageEventBroker broker;

    @Before
    public void setUp() {
        broker = new ArtemisStatusMessageEventBroker(resource.getVmURL(), null, null);
//        broker = new ArtemisStatusMessageEventBroker("tcp://localhost:61616", "admin", "admin");
    }

    @Test
    public void test_should_pass() throws Exception {
        //given
        UUID key = UUID.randomUUID();

        List<String> output = new ArrayList<>();
        broker.setConsumer(key, output::add);

        //when
        broker.send(new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_CREATE));
        // Send a message with another ID
        broker.send(new StatusMessageEvent(UUID.randomUUID(), LauncherStatusEventKind.GITHUB_WEBHOOK));
        broker.send(new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_PUSHED));
        broker.send(new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_WEBHOOK));

        //then
        List<String> expected = Arrays.asList(
                JsonUtils.toString(new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_CREATE)),
                JsonUtils.toString(new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_PUSHED)),
                JsonUtils.toString(new StatusMessageEvent(key, LauncherStatusEventKind.GITHUB_WEBHOOK))
        );
        //Sleep a bit to ensure all messages are received
        Thread.sleep(300);
        assertThat(output).containsExactlyElementsOf(expected);
    }

    @After
    public void tearDown() {
        broker.close();
    }
}