package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import io.fabric8.launcher.web.endpoints.inputs.LaunchProjectileInput;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectileInput;

import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launcher")
@RequestScoped
public class LaunchEndpoint {

    private static final String PATH_STATUS = "/status";

    private static final String APPLICATION_ZIP = "application/zip";

    private static Logger log = Logger.getLogger(LaunchEndpoint.class.getName());

    @Inject
    MissionControl missionControl;

    @Inject
    private Event<StatusMessageEvent> event;

    @Inject
    private DirectoryReaper reaper;

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
    @Produces(MediaType.APPLICATION_JSON)
    public void launch(@Valid @BeanParam LaunchProjectileInput launchProjectileInput, @Suspended AsyncResponse response) {
        CreateProjectile projectile;
        try {
            projectile = (CreateProjectile) missionControl.prepare(launchProjectileInput);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        // No need to hold off the processing, return the status link immediately
        response.resume(createObjectBuilder()
                                .add("uuid", projectile.getId().toString())
                                .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                                .build());
        projectile = ImmutableLauncherCreateProjectile.builder()
                .from(projectile)
                .startOfStep(launchProjectileInput.getExecutionStep())
                .eventConsumer(event::fire)
                .build();
        try {
            log.info("Launching projectile " + projectile);
            missionControl.launch(projectile);
            log.info("Projectile " + projectile.getId() + " launched");
        } catch (Exception ex) {
            log.log(Level.WARNING, "Projectile " + projectile.getId() + " failed to launch", ex);
            projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), ex));
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }
}