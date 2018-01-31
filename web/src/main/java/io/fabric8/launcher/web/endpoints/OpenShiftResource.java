package io.fabric8.launcher.web.endpoints;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;
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
    private Instance<KeycloakService> keycloakServiceInstance;


    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                                   @Context HttpServletRequest request) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (request.getParameterMap().containsKey("all") || openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // TODO: Remove this since getAllOpenShiftClusters already does this
            // Return all clusters
            clusters
                    .stream()
                    .map(OpenShiftCluster::getId)
                    .forEach(arrayBuilder::add);
        } else {
            final KeycloakService keycloakService = this.keycloakServiceInstance.get();
            clusters.parallelStream().map(OpenShiftCluster::getId)
                    .forEach(clusterId ->
                                     keycloakService.getIdentity(clusterId, authorization)
                                             .ifPresent(token -> arrayBuilder.add(clusterId)));
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
}