package io.openshift.launchpad.backend.web.api;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Reports that the application is available to receive requests
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(HealthResource.PATH_HEALTH)
@ApplicationScoped
public class HealthResource {

    public static final String PATH_HEALTH = "/health";
    public static final String PATH_READY = "/ready";

    private static final String STATUS = "status";
    private static final String OK = "OK";

    /**
     * Returns a JSON object with a single attribute,
     * {@link HealthResource#STATUS}, with a value of
     * {@link HealthResource#OK} to show that we are ready to receive requests
     *
     * @return
     */
    @GET
    @Path(HealthResource.PATH_READY)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject ready() {
        return Json.createObjectBuilder().
                add(STATUS, OK).
                build();
    }
}
