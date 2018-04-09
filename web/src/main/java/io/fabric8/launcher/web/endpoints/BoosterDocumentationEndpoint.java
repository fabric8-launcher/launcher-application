package io.fabric8.launcher.web.endpoints;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.core.api.documentation.BoosterDocumentationStore;

@Path("/booster-documentation")
@ApplicationScoped
public class BoosterDocumentationEndpoint {

    @Inject
    private BoosterDocumentationStore boosterDocumentationStore;


    /**
     * Reload the documentation. To be called once a change in the booster-documentation happens (webhook)
     */
    @POST
    @Path("/reload")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reindex(@QueryParam("token") String token) {
        // Token must match what's on the env var to proceed
        if (!Objects.equals(token, System.getenv("LAUNCHER_BACKEND_DOCUMENTATION_RELOAD_TOKEN"))) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        boosterDocumentationStore.reloadDocumentation();
        return Response.ok().build();
    }

}