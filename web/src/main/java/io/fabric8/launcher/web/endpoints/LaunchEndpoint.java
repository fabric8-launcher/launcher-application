package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.Valid;
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
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.web.endpoints.inputs.LaunchProjectile;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectile;
import io.fabric8.launcher.web.providers.DirectoryReaper;
import org.jboss.resteasy.annotations.Form;

import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launch")
@RequestScoped
public class LaunchEndpoint {

    private static final String PATH_STATUS = "/status";

    private static Logger log = Logger.getLogger(LaunchEndpoint.class.getName());

    @Inject
    MissionControl missionControl;

    @Inject
    private Event<StatusMessageEvent> event;

    @Inject
    private DirectoryReaper reaper;

    @POST
    @Path("/zip")
    @Produces("application/zip")
    public Response zip(@Valid @Form ZipProjectile zipProjectile) throws IOException {
        Projectile projectile = null;
        try {
            projectile = missionControl.prepare(zipProjectile);
            byte[] zipContents = Paths.zip(zipProjectile.getArtifactId(), projectile.getProjectLocation());
            return Response
                    .ok(zipContents)
                    .type("application/zip")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipProjectile.getArtifactId() + ".zip\"")
                    .build();
        } finally {
            if (projectile != null) {
                reaper.queueForDeletion(projectile.getProjectLocation());
            }
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void launch(@Valid @Form LaunchProjectile launchProjectile, @Suspended AsyncResponse response) {
        final Projectile projectile;
        try {
            projectile = missionControl.prepare(launchProjectile);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        // No need to hold off the processing, return the status link immediately
        response.resume(createObjectBuilder()
                                .add("uuid", projectile.getId().toString())
                                .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                                .build());
        try {
            missionControl.launch(projectile);
        } catch (Exception ex) {
            event.fire(new StatusMessageEvent(projectile.getId(), ex));
            log.log(Level.SEVERE, "Error while launching project", ex);
        } finally {
            reaper.queueForDeletion(projectile.getProjectLocation());
        }
    }

}