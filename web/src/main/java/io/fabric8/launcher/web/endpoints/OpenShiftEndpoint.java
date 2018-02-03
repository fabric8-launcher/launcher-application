package io.fabric8.launcher.web.endpoints;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
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
@RequestScoped
public class OpenShiftEndpoint {

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
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // Return all clusters
            return getAllOpenShiftClusters();
        } else {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            clusters.parallelStream()
                    .forEach(cluster ->
                                     identityProvider.getIdentity(cluster.getId(), authorization)
                                             .ifPresent(token -> arrayBuilder.add(readCluster(cluster))));
            return arrayBuilder.build();
        }
    }
    
    @GET
    @Path("/clusters/all")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getAllOpenShiftClusters() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        clusterRegistry.getClusters().stream().map(this::readCluster).forEach(arrayBuilder::add);
        // Return all clusters
        return arrayBuilder.build();
    }


    private JsonObjectBuilder readCluster(OpenShiftCluster cluster) {
        return Json.createObjectBuilder()
                .add("id", cluster.getId())
                .add("type", Objects.toString(cluster.getType(), ""));
    }
}