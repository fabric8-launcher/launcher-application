package io.fabric8.launcher.web.api;

import java.util.List;
import java.util.Set;
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

import io.fabric8.launcher.base.identity.ImmutableTokenIdentity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @deprecated replaced by {@link io.fabric8.launcher.web.endpoints.OpenShiftEndpoint}
 */
@Deprecated
@Path(OpenShiftResource.PATH_RESOURCE)
@ApplicationScoped
public class OpenShiftResource {

    private static final String OSIO_CLUSTER_TYPE = "osio";

    static final String PATH_RESOURCE = "/openshift";

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @Inject
    private Instance<IdentityProvider> identityProviderInstance;

    @Inject
    private Instance<TokenIdentity> authorizationInstance;

    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters(@Context HttpServletRequest request, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (request.getParameterMap().containsKey("all") || openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // TODO: Remove this since getAllOpenShiftClusters already does this
            // Return all clusters
            clusters
                    .stream()
                    .filter(b -> !OSIO_CLUSTER_TYPE.equalsIgnoreCase(b.getType()))
                    .map(OpenShiftCluster::getId)
                    .forEach(arrayBuilder::add);
        } else if (!isBearerAuthentication(authorizationHeader)) {
            return arrayBuilder.build();
        } else {
            final IdentityProvider identityProvider = this.identityProviderInstance.get();
            final TokenIdentity immutableAuthorization = ImmutableTokenIdentity.copyOf(authorizationInstance.get());
            clusters.stream()
                    .filter(b -> !OSIO_CLUSTER_TYPE.equalsIgnoreCase(b.getType()))
                    .map(OpenShiftCluster::getId)
                    .forEach(clusterId -> identityProvider.getIdentity(immutableAuthorization, clusterId)
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
        return clusters.stream()
                .filter(b -> !OSIO_CLUSTER_TYPE.equalsIgnoreCase(b.getType()))
                .map(OpenShiftCluster::getId).collect(Collectors.toList());
    }
}