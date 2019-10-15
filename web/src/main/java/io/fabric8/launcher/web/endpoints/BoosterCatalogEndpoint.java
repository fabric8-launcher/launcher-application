package io.fabric8.launcher.web.endpoints;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.catalog.BoosterCatalogFactory;

import static io.fabric8.launcher.base.JsonUtils.createArrayNode;
import static io.fabric8.launcher.base.JsonUtils.createObjectNode;
import static io.fabric8.launcher.base.JsonUtils.toObjectNode;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withParameters;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRuntime;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/booster-catalog")
@ApplicationScoped
public class BoosterCatalogEndpoint {

    @Inject
    BoosterCatalogFactory boosterCatalogFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("squid:S3776")
    public Response getCatalog(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = getQueryParameters(uriInfo);
        RhoarBoosterCatalog catalog = boosterCatalogFactory.getBoosterCatalog();

        Predicate<RhoarBooster> filter = withParameters(parameters);

        final ObjectNode response = createObjectNode();
        final ArrayNode boosterArray = createArrayNode();

        for (RhoarBooster b : catalog.getBoosters(filter)) {
            Map<String, Object> data = b.getExportableData();
            boosterArray.add(toObjectNode(data));
        }
        response.set("boosters", boosterArray);

        final ArrayNode runtimeArray = createArrayNode();
        // Add runtimes
        for (Runtime r : catalog.getRuntimes(filter)) {
            ObjectNode runtime = createObjectNode()
                    .put("id", r.getId())
                    .put("name", r.getName());
            if (r.getIcon() != null) {
                runtime.put("icon", r.getIcon());
            }
            if (r.getDescription() != null) {
                runtime.put("description", r.getDescription());
            }
            if (!r.getMetadata().isEmpty()) {
                runtime.set("metadata", toObjectNode(r.getMetadata()));
            }

            //Add versions
            final ArrayNode versionArray = createArrayNode();
            for (Version v : catalog.getVersions(filter.and(withRuntime(r)))) {
                ObjectNode version = createObjectNode()
                        .put("id", v.getId())
                        .put("name", v.getName());
                if (!v.getMetadata().isEmpty()) {
                    version.set("metadata", toObjectNode(v.getMetadata()));
                }
                versionArray.add(version);
            }
            runtime.set("versions", versionArray);

            runtimeArray.add(runtime);
        }
        response.set("runtimes", runtimeArray);

        // Add missions
        final ArrayNode missionArray = createArrayNode();
        for (Mission m : catalog.getMissions(filter)) {
            ObjectNode mission = createObjectNode()
                    .put("id", m.getId())
                    .put("name", m.getName());
            if (m.getDescription() != null) {
                mission.put("description", m.getDescription());
            }
            if (!m.getMetadata().isEmpty()) {
                mission.set("metadata", toObjectNode(m.getMetadata()));
            }
            missionArray.add(mission);
        }
        response.set("missions", missionArray);

        return Response.ok(response).build();
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
        if (boosterCatalogFactory.isIndexing()) {
            return Response.status(Response.Status.NOT_MODIFIED).build();
        } else {
            boosterCatalogFactory.reset();
        }
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