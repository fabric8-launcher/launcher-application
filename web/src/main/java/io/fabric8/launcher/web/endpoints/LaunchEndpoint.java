package io.fabric8.launcher.web.endpoints;

import java.io.IOException;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.web.endpoints.inputs.LaunchProjectile;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectile;
import org.jboss.resteasy.annotations.Form;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launch")
@RequestScoped
public class LaunchEndpoint {

    @Resource
    ManagedExecutorService executor;

    @Inject
    MissionControl missionControl;

    @POST
    @Path("/zip")
    @Produces("application/zip")
    public Response zip(@Valid @Form ZipProjectile zipProjectile) throws IOException {
        Projectile projectile = missionControl.prepare(zipProjectile);
        byte[] zipContents = Paths.zip(zipProjectile.getArtifactId(), projectile.getProjectLocation());
        return Response
                .ok(zipContents)
                .type("application/zip")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipProjectile.getArtifactId() + ".zip\"")
                .build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response launch(@Valid @Form LaunchProjectile launchProjectile) {
        try {
            Projectile projectile = missionControl.prepare(launchProjectile);
            Boom boom = missionControl.launch(projectile);
            return Response.ok(boom).build();
        } finally {
            // TODO: Mark directory for deletion
        }
    }

}