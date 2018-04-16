package io.fabric8.launcher.osio.web.endpoints;

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

import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import io.fabric8.launcher.osio.OsioMissionControl;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;
import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;
import org.apache.commons.lang3.time.StopWatch;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/osio")
@RequestScoped
public class OsioEndpoint {

    private static final String PATH_STATUS = "/status";

    @Inject
    @Application(OSIO)
    private OsioMissionControl missionControl;

    @Inject
    private DirectoryReaper reaper;

    private static Logger log = Logger.getLogger(OsioEndpoint.class.getName());

    @POST
    @Path("/launch")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public void launch(@Valid @BeanParam OsioProjectileContext context, @Suspended AsyncResponse response) {
        final OsioLaunchProjectile projectile;
        try {
            projectile = missionControl.prepare(context);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        // No need to hold off the processing, return the status link immediately
        response.resume(createObjectBuilder()
                                .add("uuid", projectile.getId().toString())
                                .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                                .build());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            log.info("Launching OSIO projectile " + projectile);
            missionControl.launch(projectile);
            stopWatch.stop();
            log.info("OSIO Projectile " + projectile.getId() + " launched. Time Elapsed: " + stopWatch);
        } catch (Exception ex) {
            stopWatch.stop();
            log.log(Level.WARNING, "OSIO Projectile " + projectile + " failed to launch. Time Elapsed: " + stopWatch, ex);
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public void importRepository(@Valid @BeanParam OsioImportProjectileContext context, @Suspended AsyncResponse response) {
        OsioImportProjectile projectile = missionControl.prepareImport(context);
        // No need to hold off the processing, return the status link immediately
        response.resume(createObjectBuilder()
                                .add("uuid", projectile.getId().toString())
                                .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                                .build());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            log.info("Launching OSIO Import projectile " + projectile);
            missionControl.launchImport(projectile);
            stopWatch.stop();
            log.info("OSIO Import Projectile " + projectile.getId() + " launched. Time Elapsed: " + stopWatch);
        } catch (Exception ex) {
            stopWatch.stop();
            log.log(Level.WARNING, "OSIO Import Projectile " + projectile.getId() + " failed to launch. Time Elapsed: " + stopWatch, ex);
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }
}
