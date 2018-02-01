package io.fabric8.launcher.web.endpoints;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/openshift")
@ApplicationScoped
public class OpenShiftResource {

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @Inject
    private IdentityProvider identityProvider;

    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // Return all clusters
            clusters
                    .stream()
                    .map(this::readCluster)
                    .forEach(arrayBuilder::add);
        } else {
            clusters.parallelStream()
                    .forEach(cluster ->
                                     identityProvider.getIdentity(cluster.getId(), authorization)
                                             .ifPresent(token -> arrayBuilder.add(readCluster(cluster))));
        }

        return arrayBuilder.build();
    }

    @GET
    @Path("/clusters/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllOpenShiftClusters() {
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        // Return all clusters
        return clusters.stream().map(OpenShiftCluster::getId).collect(Collectors.toList());
    }


    private JsonObjectBuilder readCluster(OpenShiftCluster cluster) {
        return Json.createObjectBuilder()
                .add("id", cluster.getId())
                .add("type", Objects.toString(cluster.getType(), ""));
    }
}