package io.fabric8.launcher.web.endpoints;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.catalog.BoosterCatalogFactory;

import static io.fabric8.launcher.base.JsonUtils.toJsonObjectBuilder;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withAppEnabled;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withParameters;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRuntime;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/booster-catalog")
@ApplicationScoped
public class BoosterCatalogEndpoint {

    private static final String HEADER_APP = "X-App";

    @Inject
    private BoosterCatalogFactory boosterCatalogFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("squid:S3776")
    public Response getCatalog(@HeaderParam(HEADER_APP) String application, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = getQueryParameters(uriInfo);
        RhoarBoosterCatalog catalog = boosterCatalogFactory.getBoosterCatalog();

        Predicate<RhoarBooster> filter = withAppEnabled(application).and(withParameters(parameters));

        JsonObjectBuilder response = createObjectBuilder();
        JsonArrayBuilder boosterArray = createArrayBuilder();
        // Remove environment entry
        for (RhoarBooster b : catalog.getBoosters(filter)) {
            Map<String, Object> data = b.getExportableData();
            data.remove("environment");
            boosterArray.add(toJsonObjectBuilder(data));
        }
        response.add("boosters", boosterArray);

        JsonArrayBuilder runtimeArray = createArrayBuilder();
        // Add runtimes
        for (Runtime r : catalog.getRuntimes(filter)) {
            JsonObjectBuilder runtime = createObjectBuilder()
                    .add("id", r.getId())
                    .add("name", r.getName());
            if (r.getIcon() != null) {
                runtime.add("icon", r.getIcon());
            }
            if (r.getDescription() != null) {
                runtime.add("description", r.getDescription());
            }
            if (!r.getMetadata().isEmpty()) {
                runtime.add("metadata", toJsonObjectBuilder(r.getMetadata()));
            }

            //Add versions
            JsonArrayBuilder versionArray = createArrayBuilder();
            for (Version v : catalog.getVersions(filter.and(withRuntime(r)))) {
                JsonObjectBuilder version = createObjectBuilder()
                        .add("id", v.getId())
                        .add("name", v.getName());
                if (v.getDescription() != null) {
                    version.add("description", v.getDescription());
                }
                if (!v.getMetadata().isEmpty()) {
                    version.add("metadata", toJsonObjectBuilder(v.getMetadata()));
                }
                versionArray.add(version);
            }
            runtime.add("versions", versionArray);

            runtimeArray.add(runtime);
        }
        response.add("runtimes", runtimeArray);

        // Add missions
        JsonArrayBuilder missionArray = createArrayBuilder();
        for (Mission m : catalog.getMissions(filter)) {
            JsonObjectBuilder mission = createObjectBuilder()
                    .add("id", m.getId())
                    .add("name", m.getName());
            if (m.getDescription() != null) {
                mission.add("description", m.getDescription());
            }
            if (!m.getMetadata().isEmpty()) {
                mission.add("metadata", toJsonObjectBuilder(m.getMetadata()));
            }
            missionArray.add(mission);
        }
        response.add("missions", missionArray);

        return Response.ok(response.build()).build();
    }

    /**
     * Reindexes the catalog. To be called once a change in the booster-catalog happens (webhook)
     */
    @POST
    @Path("/reindex")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reindex(@QueryParam("token") String token) {
        // Token must match what's on the env var to proceed
        if (!Objects.equals(token, System.getenv("LAUNCHER_BACKEND_CATALOG_REINDEX_TOKEN"))) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        boosterCatalogFactory.reset();
        return Response.ok().build();
    }

    /**
     * Used in integration tests
     */
    @GET
    @Path("/wait")
    public Response waitForIndex() throws InterruptedException, ExecutionException {
        boosterCatalogFactory.waitForIndex();
        return Response.ok().build();
    }

    /**
     * @return the query parameters (without the "runsOn" parameter if that exists)
     */
    private MultivaluedMap<String, String> getQueryParameters(UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (queryParameters.containsKey("runsOn")) {
            MultivaluedMap<String, String> fixed = new MultivaluedHashMap<>();
            fixed.putAll(queryParameters);
            fixed.remove("runsOn");
            return fixed;
        } else {
            return queryParameters;
        }
    }
}