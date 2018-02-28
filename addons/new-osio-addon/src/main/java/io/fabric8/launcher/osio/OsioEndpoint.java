package io.fabric8.launcher.osio;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.DirectoryReaper;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.projectiles.ImmutableImportProjectile;
import io.fabric8.launcher.osio.projectiles.ImportProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectileContext;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/osio")
public class OsioEndpoint {

    private static final String PATH_STATUS = "/status";

    @Inject
    @Application(OSIO)
    private OsioMissionControl missionControl;

    @Inject
    private DirectoryReaper reaper;

    @Inject
    private Event<StatusMessageEvent> event;

    private static Logger log = Logger.getLogger(OsioEndpoint.class.getName());

    @POST
    @Path("/launch")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public void launch(@Valid @BeanParam OsioProjectileContext context, @Suspended AsyncResponse response) {
        final Projectile projectile;
        try {
            projectile = missionControl.prepare(context);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        if (!(projectile instanceof CreateProjectile)) {
            throw new IllegalStateException("Projectile prepared is not an instance of " + CreateProjectile.class.getName());
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
            reaper.delete(projectile.getProjectLocation());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void importRepository(@FormParam("gitOrganization") String gitOrganization,
                                 @FormParam("gitRepository") String gitRepository) {
        ImportProjectile projectile = ImmutableImportProjectile.builder()
                .gitOrganization(gitOrganization)
                .gitRepository(gitRepository)
                .build();
        missionControl.launch(projectile);
    }
}
