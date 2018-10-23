package io.fabric8.launcher.core.impl.producers;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.impl.events.ArtemisStatusMessageEventBroker;
import io.fabric8.launcher.core.impl.events.LocalStatusMessageEventBroker;

import static io.fabric8.launcher.core.impl.CoreEnvironment.ARTEMIS_PASSWORD;
import static io.fabric8.launcher.core.impl.CoreEnvironment.ARTEMIS_URL;
import static io.fabric8.launcher.core.impl.CoreEnvironment.ARTEMIS_USER;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class StatusMessageEventBrokerProducer {

    private static final Logger logger = Logger.getLogger(StatusMessageEventBrokerProducer.class.getName());

    @Produces
    @ApplicationScoped
    public StatusMessageEventBroker produceEventBroker() {
        String artemisUrl = ARTEMIS_URL.value();
        if (artemisUrl == null) {
            logger.info("Handling status messages through " + LocalStatusMessageEventBroker.class.getSimpleName());
            return new LocalStatusMessageEventBroker();
        } else {
            String user = ARTEMIS_USER.value();
            String password = ARTEMIS_PASSWORD.value();
            logger.info("Handling status messages through " + ArtemisStatusMessageEventBroker.class.getSimpleName());
            return new ArtemisStatusMessageEventBroker(artemisUrl, user, password);
        }
    }

    public void destroyEventBroker(@Disposes StatusMessageEventBroker broker) {
        // Ensure this is called
        broker.close();
    }
}
