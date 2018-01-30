package io.fabric8.launcher.web.endpoints;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;

import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withMission;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRuntime;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(BoosterCatalogResource.PATH)
@ApplicationScoped
public class BoosterCatalogResource {

    public static final String PATH = "/booster-catalog";

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
                    .add("icon", r.getIcon());
            for (Mission m : catalog.getMissions(withRuntime(r))) {
                JsonArrayBuilder versions = createArrayBuilder();
                missions.add(createObjectBuilder()
                                     .add("id", m.getId())
                                     .add("versions", versions));
                for (Version version : catalog.getVersions(m, r)) {
                    versions.add(createObjectBuilder()
                                         .add("id", version.getId())
                                         .add("name", version.getName()));
                }
            }
            runtime.add("missions", missions);
            response.add(runtime);
        }
        return Response.ok(response.build()).build();

    }
}
