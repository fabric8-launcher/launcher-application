package io.fabric8.launcher.web.endpoints;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/openshift")
@RequestScoped
public class OpenShiftEndpoint {

    private static final String OSIO_CLUSTER_TYPE = "osio";


    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @Inject
    private IdentityProvider identityProvider;

    @Inject
    private Instance<OpenShiftService> openShiftService;

    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters() {
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // Return all clusters
            return getAllOpenShiftClusters();
        } else {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            clusters.parallelStream()
                    .filter(b -> !OSIO_CLUSTER_TYPE.equalsIgnoreCase(b.getType()))
                    .forEach(cluster ->
                                     identityProvider.getIdentity(cluster.getId())
                                             .ifPresent(token -> arrayBuilder.add(readCluster(cluster))));
            return arrayBuilder.build();
        }
    }

    @GET
    @Path("/clusters/all")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getAllOpenShiftClusters() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        clusterRegistry.getClusters().stream()
                .filter(b -> !OSIO_CLUSTER_TYPE.equalsIgnoreCase(b.getType()))
                .map(this::readCluster).forEach(arrayBuilder::add);
        // Return all clusters
        return arrayBuilder.build();
    }


    @HEAD
    @Path("/projects/{project}")
    public Response openShiftProjectExists(@NotNull @PathParam("project") String project) {
        if (openShiftService.get().projectExists(project)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    private JsonObjectBuilder readCluster(OpenShiftCluster cluster) {
        return Json.createObjectBuilder()
                .add("id", cluster.getId())
                .add("type", Objects.toString(cluster.getType(), ""));
    }
}