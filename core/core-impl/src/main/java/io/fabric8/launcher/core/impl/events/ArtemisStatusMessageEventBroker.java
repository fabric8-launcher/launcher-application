package io.fabric8.launcher.core.impl.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Topic;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import static io.fabric8.launcher.core.impl.CoreEnvVarSysPropNames.HOSTNAME;


/**
 * A {@link StatusMessageEventBroker} implementation that uses ActiveMQ Artemis
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ArtemisStatusMessageEventBroker implements StatusMessageEventBroker {

    private static final Logger logger = Logger.getLogger(ArtemisStatusMessageEventBroker.class.getName());

    private static final String DESTINATION_NAME = "launcher-status-messages";

    private final Topic destination;

    private final ActiveMQConnectionFactory connectionFactory;

    private final Map<UUID, ConsumerTuple> consumers = new ConcurrentHashMap<>();

    public ArtemisStatusMessageEventBroker(String url, String user, String password) {
        connectionFactory = new ActiveMQConnectionFactory(url, user, password);
        destination = ActiveMQJMSClient.createTopic(DESTINATION_NAME);
    }

    @Override
    public void setConsumer(UUID key, Consumer<String> consumer) {
        consumers.compute(key, (uuid, tuple) -> {
            if (tuple != null) {
                tuple.close();
            }
            JMSContext jmsContext = connectionFactory.createContext();
            String sharedSubscriptionName = HOSTNAME.value("localhost") + "-" + key;
            String messageSelector = "msgId='" + key + "'";
            JMSConsumer jmsConsumer = jmsContext.createSharedConsumer(destination, sharedSubscriptionName, messageSelector);
            jmsConsumer.setMessageListener(message -> {
                try {
                    consumer.accept(message.getBody(String.class));
                } catch (JMSException e) {
                    logger.log(Level.WARNING, "Error while consuming JMS message", e);
                }
            });
            return new ConsumerTuple(jmsContext, jmsConsumer);
        });
    }

    @Override
    public void removeConsumer(UUID key) {
        ConsumerTuple tuple = consumers.remove(key);
        if (tuple != null) {
            tuple.close();
        }
    }

    @Override
    public void send(StatusMessageEvent event) {
        try (JMSContext jmsContext = createContext()) {
            JMSProducer producer = jmsContext.createProducer();
            String message;
            try {
                message = JsonUtils.toString(event);
            } catch (IOException e) {
                // Should never happen
                throw new UncheckedIOException(e);
            }
            producer
                    .setDisableMessageID(true)
                    .setDisableMessageTimestamp(true)
                    // 60 secs
                    .setTimeToLive(60000)
                    .setProperty("msgId", event.getId().toString())
                    .send(destination, message);
        }
    }

    private JMSContext createContext() {
        JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
        context.setClientID(HOSTNAME.value("localhost"));
        return context;
    }

    @Override
    public void close() {
        consumers.values().forEach(ConsumerTuple::close);
        consumers.clear();
        connectionFactory.close();
    }


    /**
     * Needed to cleanup resources later
     */
    private class ConsumerTuple {
        private final JMSContext context;

        private final JMSConsumer consumer;

        ConsumerTuple(JMSContext context, JMSConsumer consumer) {
            this.context = context;
            this.consumer = consumer;
        }

        public void close() {
            consumer.close();
            context.stop();
            context.close();
        }
    }


}
