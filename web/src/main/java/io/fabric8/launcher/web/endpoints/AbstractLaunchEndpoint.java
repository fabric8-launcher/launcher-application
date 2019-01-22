package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.AsyncResponse;

import io.fabric8.launcher.core.api.ImmutableAsyncBoom;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.events.StatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import org.apache.commons.lang3.time.StopWatch;

public abstract class AbstractLaunchEndpoint {

    private static final Logger log = Logger.getLogger("io.fabric8.launcher.web.endpoints.launch");

    @Inject
    protected DirectoryReaper reaper;

    /**
     * Performs the launch
     */
    protected <P extends Projectile> void doLaunch(P projectile, Consumer<P> handler, Collection<StatusEventKind> events,
                                                   HttpServletResponse response, AsyncResponse asyncResponse) throws IOException {
        // No need to hold off the processing, return the status link immediately
        // Need to close the response's OutputStream after resuming to automatically flush the contents
        try (ServletOutputStream stream = response.getOutputStream()) {
            asyncResponse.resume(ImmutableAsyncBoom.builder()
                                         .uuid(projectile.getId())
                                         .eventTypes(events)
                                         .build());
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            log.log(Level.INFO, "Launching projectile {0}", projectile);
            handler.accept(projectile);
            stopWatch.stop();
            log.log(Level.INFO, "Projectile {0} launched. Time Elapsed: {1}", new Object[]{projectile.getId(), stopWatch});
        } catch (Exception ex) {
            stopWatch.stop();
            log.log(Level.WARNING, "Projectile " + projectile + " failed to launch. Time Elapsed: " + stopWatch, ex);
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
        }
    }

}
