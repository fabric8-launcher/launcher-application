package io.fabric8.launcher.osio.web.endpoints;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.core.api.ImmutableAsyncBoom;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import io.fabric8.launcher.osio.OsioImportMissionControl;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;
import org.apache.commons.lang3.time.StopWatch;

import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_PUSHED;
import static io.fabric8.launcher.core.api.events.StatusEventType.GITHUB_WEBHOOK;
import static io.fabric8.launcher.core.api.events.StatusEventType.OPENSHIFT_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.OPENSHIFT_PIPELINE;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/osio/import")
@RequestScoped
public class OsioImportEndpoint {

    @Inject
    private OsioImportMissionControl missionControl;

    @Inject
    private DirectoryReaper reaper;

    private static Logger log = Logger.getLogger(OsioImportEndpoint.class.getName());

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public void importRepository(@Valid @BeanParam OsioImportProjectileContext context, @Suspended AsyncResponse response) {
        OsioImportProjectile projectile = missionControl.prepare(context);

        // No need to hold off the processing, return the status link immediately
        response.resume(ImmutableAsyncBoom.builder()
                                .uuid(projectile.getId())
                                .eventTypes(EnumSet.of(OPENSHIFT_CREATE, GITHUB_WEBHOOK, GITHUB_PUSHED, OPENSHIFT_PIPELINE))
                                .build());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            log.log(Level.INFO, "Launching OSIO Import projectile {0}", projectile);
            missionControl.launch(projectile);
            stopWatch.stop();
            log.log(Level.INFO, "OSIO Import Projectile {0} launched. Time Elapsed: {1}", new Object[]{projectile.getId(), stopWatch});
        } catch (Exception ex) {
            stopWatch.stop();
            log.log(Level.WARNING, "OSIO Import Projectile " + projectile.getId() + " failed to launch. Time Elapsed: " + stopWatch, ex);
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }
}
