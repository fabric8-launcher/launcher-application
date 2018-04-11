package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.events.StatusEventType;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import io.fabric8.launcher.web.endpoints.inputs.LaunchProjectileInput;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectileInput;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launcher")
@RequestScoped
public class LaunchEndpoint {

    private static final String APPLICATION_ZIP = "application/zip";

    private static Logger log = Logger.getLogger(LaunchEndpoint.class.getName());

    @Inject
    MissionControl missionControl;

    @Inject
    private Event<StatusMessageEvent> event;

    @Inject
    private DirectoryReaper reaper;

    @Context
    HttpServletResponse response;

    @GET
    @Path("/zip")
    @Produces(APPLICATION_ZIP)
    public Response zip(@Valid @BeanParam ZipProjectileInput zipProjectile) throws IOException {
        CreateProjectile projectile = null;
        try {
            projectile = (CreateProjectile) missionControl.prepare(zipProjectile);
            byte[] zipContents = Paths.zip(zipProjectile.getArtifactId(), projectile.getProjectLocation());
            return Response
                    .ok(zipContents)
                    .type(APPLICATION_ZIP)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipProjectile.getArtifactId() + ".zip\"")
                    .build();
        } finally {
            if (projectile != null) {
                reaper.delete(projectile.getProjectLocation());
            }
        }
    }

    @POST
    @Path("/launch")
    @Secured
    public void launch(@Valid @BeanParam LaunchProjectileInput launchProjectileInput) throws IOException {
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

        final CreateProjectile projectile;
        try {
            projectile = (CreateProjectile) missionControl.prepare(launchProjectileInput);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        try {
            CreateProjectile projectileWithStep = ImmutableLauncherCreateProjectile.builder()
                    .from(projectile)
                    .startOfStep(launchProjectileInput.getExecutionStep())
                    .eventConsumer(this::onEvent)
                    .build();
            missionControl.launch(projectileWithStep);
        } catch (Exception ex) {
            event.fire(new StatusMessageEvent(projectile.getId(), ex));
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