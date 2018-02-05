package io.fabric8.launcher.osio.importing;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.osio.OsioMissionControl;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launch/osio/import")
public class ImportEndpoint {

    @Inject
    private OsioMissionControl control;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void importRepository(@FormParam("gitOrganization") String gitOrganization,
                                 @FormParam("gitRepository") String gitRepository) {
        ImportProjectile projectile = ImmutableImportProjectile.builder()
                .gitOrganization(gitOrganization)
                .gitRepository(gitRepository)
                .build();
        control.importRepository(projectile);
    }
}
