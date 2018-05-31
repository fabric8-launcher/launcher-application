package io.fabric8.launcher.web.endpoints;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.catalog.BoosterCatalogFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static io.fabric8.launcher.base.JsonUtils.toJsonObjectBuilder;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withAppEnabled;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withMission;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withParameters;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRunsOn;
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
    @Path("/missions")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response getMissions(@HeaderParam(HEADER_APP) String application, @QueryParam("runsOn") String runsOn, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = fixParamMap(uriInfo.getQueryParameters());
        RhoarBoosterCatalog catalog = boosterCatalogFactory.getBoosterCatalog();
        Predicate<RhoarBooster> filter = withAppEnabled(application).and(withRunsOn(runsOn)).and(withParameters(parameters));
        JsonArrayBuilder response = createArrayBuilder();
        for (Mission m : catalog.getMissions(filter)) {
            JsonArrayBuilder runtimes = createArrayBuilder();
            JsonObjectBuilder mission = createObjectBuilder()
                    .add("id", m.getId())
                    .add("name", m.getName());
            if (m.getDescription() != null) {
                mission.add("description", m.getDescription());
            }
            if (!m.getMetadata().isEmpty()) {
                mission.add("metadata", toJsonObjectBuilder(m.getMetadata()));
            }

            // Add all runtimes
            catalog.getRuntimes(filter.and(withMission(m)))
                    .stream()
                    .map(Runtime::getId)
                    .forEach(runtimes::add);

            mission.add("runtimes", runtimes);
            response.add(mission);
        }
        return Response.ok(response.build()).build();
    }

    @GET
    @Path("/runtimes")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response getRuntime(@HeaderParam(HEADER_APP) String application, @QueryParam("runsOn") String runsOn, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = fixParamMap(uriInfo.getQueryParameters());
        RhoarBoosterCatalog catalog = boosterCatalogFactory.getBoosterCatalog();
        Predicate<RhoarBooster> filter = withAppEnabled(application).and(withRunsOn(runsOn)).and(withParameters(parameters));
        JsonArrayBuilder response = createArrayBuilder();
        for (Runtime r : catalog.getRuntimes(filter)) {
            JsonArrayBuilder missions = createArrayBuilder();
            JsonObjectBuilder runtime = createObjectBuilder()
                    .add("id", r.getId())
                    .add("name", r.getName())
                    .add("icon", r.getIcon());
            if (r.getDescription() != null) {
                runtime.add("description", r.getDescription());
            }
            if (!r.getMetadata().isEmpty()) {
                runtime.add("metadata", toJsonObjectBuilder(r.getMetadata()));
            }
            for (Mission m : catalog.getMissions(filter.and(withRuntime(r)))) {
                JsonArrayBuilder versions = createArrayBuilder();
                JsonObjectBuilder mission = createObjectBuilder()
                        .add("id", m.getId());
                for (Version v : catalog.getVersions(filter.and(withMission(m)).and(withRuntime(r)))) {
                    JsonObjectBuilder version = createObjectBuilder()
                            .add("id", v.getId())
                            .add("name", v.getName());
                    if (v.getDescription() != null) {
                        version.add("description", v.getDescription());
                    }
                    if (v.getMetadata() != null && !v.getMetadata().isEmpty()) {
                        version.add("metadata", toJsonObjectBuilder(v.getMetadata()));
                    }
                    catalog.getBooster(m, r, v).ifPresent(booster -> {
                        JsonObjectBuilder boosterBuilder = createObjectBuilder();
                        if (!booster.getMetadata().isEmpty()) {
                            boosterBuilder.add("metadata", toJsonObjectBuilder(booster.getMetadata()));
                        }
                        version.add("booster", boosterBuilder);
                    });
                    versions.add(version);
                }
                mission.add("versions", versions);
                missions.add(mission);
            }
            runtime.add("missions", missions);
            response.add(runtime);
        }
        return Response.ok(response.build()).build();
    }

    private MultivaluedMap<String,String> fixParamMap(MultivaluedMap<String,String> queryParameters) {
        if (queryParameters.containsKey("runsOn")) {
            MultivaluedMap<String, String> fixed = new MultivaluedHashMap<>();
            fixed.putAll(queryParameters);
            fixed.remove("runsOn");
            return fixed;
        } else {
            return queryParameters;
        }
    }

    @GET
    @Path("/booster")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response getBoosters(@NotNull(message = "mission is required") @QueryParam("mission") Mission mission,
                                @NotNull(message = "runtime is required") @QueryParam("runtime") Runtime runtime,
                                @QueryParam("runtimeVersion") Version version) {
        RhoarBoosterCatalog catalog = boosterCatalogFactory.getBoosterCatalog();
        Optional<RhoarBooster> result = catalog.getBooster(mission, runtime, version);

        return result.map(b -> {
            JsonObjectBuilder booster = createObjectBuilder()
                    .add("id", b.getId());

            booster.add("gitRepo", b.getGitRepo());
            booster.add("gitRef", b.getGitRef());

            if (!b.getMetadata().isEmpty()) {
                booster.add("metadata", toJsonObjectBuilder(b.getMetadata()));
            }

            if (b.getMission() != null && !b.getMission().getMetadata().isEmpty()) {
                booster.add("mission", createObjectBuilder().add("metadata", toJsonObjectBuilder(b.getMission().getMetadata())));
            }

            if (b.getRuntime() != null && !b.getRuntime().getMetadata().isEmpty()) {
                booster.add("runtime", createObjectBuilder().add("metadata", toJsonObjectBuilder(b.getRuntime().getMetadata())));
            }
            return Response.ok(booster.build()).build();
        }).orElseThrow(NotFoundException::new);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalog(@HeaderParam(HEADER_APP) String application, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = fixParamMap(uriInfo.getQueryParameters());
        RhoarBoosterCatalog catalog = boosterCatalogFactory.getBoosterCatalog();

        Predicate<RhoarBooster> filter = withAppEnabled(application).and(withParameters(parameters));

        JsonObjectBuilder response = createObjectBuilder();
        JsonArrayBuilder boosterArray = createArrayBuilder();
        for (RhoarBooster b : catalog.getBoosters(filter)) {
            Map<String, Object> data = b.getExportableData();
            data.remove("environment");
            boosterArray.add(toJsonObjectBuilder(data));
        }
        response.add("boosters", boosterArray);

        JsonArrayBuilder runtimeArray = createArrayBuilder();
        for (Runtime r : catalog.getRuntimes(filter)) {
            JsonObjectBuilder runtime = createObjectBuilder()
                    .add("id", r.getId())
                    .add("name", r.getName())
                    .add("icon", r.getIcon());
            if (r.getDescription() != null) {
                runtime.add("description", r.getDescription());
            }
            if (!r.getMetadata().isEmpty()) {
                runtime.add("metadata", toJsonObjectBuilder(r.getMetadata()));
            }

            // TODO: Decide if we really want this here or not
            JsonArrayBuilder versionArray = createArrayBuilder();
            for (Version v : catalog.getVersions(filter.and(withRuntime(r)))) {
                JsonObjectBuilder version = createObjectBuilder()
                        .add("id", v.getId())
                        .add("name", v.getName());
                if (v.getDescription() != null) {
                    version.add("description", v.getDescription());
                }
                if (v.getMetadata() != null && !v.getMetadata().isEmpty()) {
                    version.add("metadata", toJsonObjectBuilder(v.getMetadata()));
                }
                versionArray.add(version);
            }
            runtime.add("versions", versionArray);

            runtimeArray.add(runtime);
        }
        response.add("runtimes", runtimeArray);

        JsonArrayBuilder missionArray = createArrayBuilder();
        for (Mission m : catalog.getMissions(filter)) {
            JsonArrayBuilder runtimes = createArrayBuilder();
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
    public Response waitForIndex() throws Exception {
        boosterCatalogFactory.waitForIndex();
        return Response.ok().build();
    }
}
