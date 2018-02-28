package io.fabric8.launcher.core.impl.filters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.core.api.security.Secured;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/endpoint")
public class SecuredEndpoint {


    @GET
    @Path("/secured")
    @Secured
    public Response secured() {
        return Response.ok().build();

    }

    @GET
    @Path("/insecured")
    public Response insecured() {
        return Response.ok().build();
    }
}
