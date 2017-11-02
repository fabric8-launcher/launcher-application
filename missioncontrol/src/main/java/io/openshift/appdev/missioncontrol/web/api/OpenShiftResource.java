package io.openshift.appdev.missioncontrol.web.api;

import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.service.keycloak.api.KeycloakService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(OpenShiftResource.PATH_RESOURCE)
@ApplicationScoped
public class OpenShiftResource extends AbstractResource {

    static final String PATH_RESOURCE = "/openshift";

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                                   @Context HttpServletRequest request) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (request.getParameterMap().containsKey("all") || useDefaultIdentities()) {
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
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray projectList(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                 @QueryParam("cluster") String cluster) {
        Identity openShiftIdentity = getOpenShiftIdentity(authorization, cluster);
        Optional<OpenShiftCluster> openShiftCluster = clusterRegistry.findClusterById(cluster);
        assert openShiftCluster.isPresent() : "Cluster not found: " + cluster;
        OpenShiftService openShiftService = openShiftServiceFactory.create(openShiftCluster.get(), openShiftIdentity);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        openShiftService.listProjects().stream().map(OpenShiftProject::getName).forEach(arrayBuilder::add);
        return arrayBuilder.build();
    }
}