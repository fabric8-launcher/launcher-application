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

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.web.endpoints.inputs.LaunchProjectile;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectile;
import io.fabric8.launcher.web.forge.util.Paths;
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
        CreateProjectile projectile = missionControl.prepare(zipProjectile);
        byte[] zipContents = Paths.zip("", projectile.getProjectLocation());
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
            CreateProjectile projectile = missionControl.prepare(launchProjectile);
            Boom boom = missionControl.launch(projectile);
            return Response.ok(boom).build();
        } finally {
            // TODO: Mark directory for deletion
        }
        // Grab booster from catalog
        // Copy booster content to a temp file
        // Detect project type
        //  If Maven:
        //      - Change groupId/artifactId/version and build profile
        //      - Change child modules if they exist
        //  If Node.js:
        //      - Change name and version in package.json
        // Change README
        //--------------------------------------------------
        // GITHUB_CREATE
        // GITHUB_PUSHED
        // OPENSHIFT_CREATE
        // OPENSHIFT_PIPELINE
        // GITHUB_WEBHOOK
    }

}