package io.fabric8.launcher.web.endpoints;

import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.web.endpoints.inputs.Projectile;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectile;
import org.jboss.resteasy.annotations.Form;

import static io.fabric8.launcher.web.endpoints.outputs.ImmutableBoom.of;
import static java.util.UUID.randomUUID;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launch")
@RequestScoped
public class LaunchEndpoint {

    @Resource
    ManagedExecutorService executor;

    @POST
    @Path("/zip")
    @Produces(MediaType.TEXT_PLAIN)
    public Response zip(@Valid @Form ZipProjectile zipProjectile) {
        return Response.ok(zipProjectile.toString()).build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response launch(@Valid @Form Projectile zipProjectile) {
        UUID uid = randomUUID();
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
        return Response.ok(of(uid)).build();
    }

}