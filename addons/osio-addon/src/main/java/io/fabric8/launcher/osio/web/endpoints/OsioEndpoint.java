package io.fabric8.launcher.osio.web.endpoints;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import io.fabric8.launcher.osio.OsioMissionControl;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;
import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/osio")
@RequestScoped
public class OsioEndpoint {

    private static Logger log = Logger.getLogger(OsioEndpoint.class.getName());

    @Inject
    @Application(OSIO)
    private OsioMissionControl missionControl;

    @Inject
    private DirectoryReaper reaper;

    @Context
    HttpServletResponse response;

    @POST
    @Path("/launch")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public void launch(@Valid @BeanParam OsioProjectileContext context) throws IOException {
        response.setContentType("text/event-stream");
        // Send events
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (StatusEventType statusEventType : StatusEventType.values()) {
            JsonObjectBuilder object = Json.createObjectBuilder();
            builder.add(object.add(statusEventType.name(), statusEventType.getMessage()).build());
        }
        PrintWriter writer = response.getWriter();
        writer.write(builder.build() + "\n\n");
        writer.flush();

        final OsioLaunchProjectile projectile;
        try {
            OsioLaunchProjectile preparedProjectile = missionControl.prepare(context);
            projectile = ImmutableOsioLaunchProjectile.builder()
                    .from(preparedProjectile)
                    .eventConsumer(this::onEvent)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        try {
            missionControl.launch(projectile);
        } catch (Exception ex) {
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
            log.log(Level.SEVERE, "Error while launching project", ex);
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public void importRepository(@Valid @BeanParam OsioImportProjectileContext context) throws IOException {
        response.setContentType("text/event-stream");
        // Send events
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (StatusEventType statusEventType : StatusEventType.values()) {
            JsonObjectBuilder object = Json.createObjectBuilder();
            builder.add(object.add(statusEventType.name(), statusEventType.getMessage()).build());
        }
        PrintWriter writer = response.getWriter();
        writer.write(builder.build() + "\n\n");
        writer.flush();

        OsioImportProjectile prepareImport = missionControl.prepareImport(context);
        OsioImportProjectile projectile =
                ImmutableOsioImportProjectile.builder()
                        .from(prepareImport)
                        .eventConsumer(this::onEvent)
                        .build();
        try {
            missionControl.launchImport(projectile);
        } catch (Exception ex) {
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
            log.log(Level.SEVERE, "Error while launching project", ex);
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }

    private void onEvent(StatusMessageEvent msg) {
        try {
            PrintWriter writer = response.getWriter();
            // Send contents
            String payload = JsonUtils.toString(msg);
            writer.write(payload + "\n\n");
            writer.flush();
        } catch (IOException io) {

        }
    }

}
