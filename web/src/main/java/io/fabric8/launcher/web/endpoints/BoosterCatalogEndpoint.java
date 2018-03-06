package io.fabric8.launcher.web.endpoints;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.addon.BoosterCatalogFactory;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;

import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withMission;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRuntime;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/booster-catalog")
@ApplicationScoped
public class BoosterCatalogEndpoint {

    @Inject
    private BoosterCatalogFactory boosterCatalogFactory;

    @Inject
    private RhoarBoosterCatalog catalog;

    @GET
    @Path("/missions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMissions() {
        JsonArrayBuilder response = createArrayBuilder();
        for (Mission m : catalog.getMissions()) {
            JsonArrayBuilder runtimes = createArrayBuilder();
            JsonObjectBuilder mission = createObjectBuilder()
                    .add("id", m.getId())
                    .add("name", m.getName())
                    .add("suggested", m.isSuggested());
            if (m.getDescription() != null) {
                mission.add("description", m.getDescription());
            }
            // Add all runtimes
            catalog.getRuntimes(withMission(m))
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
    public Response getRuntimes() {
        JsonArrayBuilder response = createArrayBuilder();
        for (Runtime r : catalog.getRuntimes()) {
            JsonArrayBuilder missions = createArrayBuilder();
            JsonObjectBuilder runtime = createObjectBuilder()
                    .add("id", r.getId())
                    .add("name", r.getName())
                    .add("pipelinePlatform", r.getPipelinePlatform())
                    .add("icon", r.getIcon());
            for (Mission m : catalog.getMissions(withRuntime(r))) {
                JsonArrayBuilder versions = createArrayBuilder();
                JsonObjectBuilder mission = createObjectBuilder()
                        .add("id", m.getId());
                for (Version version : catalog.getVersions(m, r)) {
                    versions.add(createObjectBuilder()
                                         .add("id", version.getId())
                                         .add("name", version.getName()));
                }
                mission.add("versions", versions);
                missions.add(mission);
            }
            runtime.add("missions", missions);
            response.add(runtime);
        }
        return Response.ok(response.build()).build();

    }

    @GET
    @Path("/booster")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBoosters(@NotNull @QueryParam("missionId") Mission mission,
                                @NotNull @QueryParam("runtimeId") Runtime runtime,
                                @QueryParam("runtimeVersion") Version version) {
        // if the version is null, getBooster(mission,runtime,version) throws an exception
        Optional<RhoarBooster> result = catalog.getBooster(mission, runtime, version);

        return result.map(b -> {
            JsonObjectBuilder booster = createObjectBuilder()
                    .add("id", b.getId());

            booster.add("gitRepo", b.getGitRepo());
            booster.add("gitRef", b.getGitRef());

            JsonArrayBuilder runsOn = createArrayBuilder();
            b.getRunsOn().forEach(runsOn::add);
            booster.add("runsOn", runsOn);

            return Response.ok(booster.build()).build();
        }).orElseThrow(NotFoundException::new);
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
        ((RhoarBoosterCatalogService) catalog).index().get();
        return Response.ok().build();
    }

}