package io.fabric8.launcher.web.endpoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.futures.CompletableFutures;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.ImmutableParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftUser;
import io.fabric8.launcher.web.producers.OpenShiftServiceProducer;

import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/openshift")
@RequestScoped
public class OpenShiftEndpoint {

    @Inject
    OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    OpenShiftClusterRegistry clusterRegistry;

    @Inject
    Instance<IdentityProvider> identityProviderInstance;

    @Inject
    Instance<OpenShiftService> openShiftService;

    @Inject
    Instance<TokenIdentity> authorizationInstance;

    @Context
    SecurityContext securityContext;

    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Collection<ClusterVerified> getSupportedOpenShiftClusters(
            @HeaderParam(OpenShiftServiceProducer.OPENSHIFT_AUTHORIZATION_HEADER) String openShiftAuth) throws ExecutionException, InterruptedException {
        final Collection<ClusterVerified> clusters;
        if (openShiftAuth != null) {
            //Test the X-OpenShift-Authorization header for every configured header
            TokenIdentity identity = TokenIdentity.fromBearerAuthorizationHeader(openShiftAuth);
            clusters = clusterRegistry.getClusters().stream()
                    .map(cluster -> getClusterVerified(cluster, identity))
                    .collect(toList());
        } else if (openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            // If the default identity is set, consider that all configured clusters are correct
            clusters = clusterRegistry.getClusters().stream()
                    .map(ClusterVerified::new)
                    .collect(toList());
        } else {
            final List<CompletableFuture<ClusterVerified>> futures = new ArrayList<>();
            IdentityProvider identityProvider = identityProviderInstance.get();
            TokenIdentity authorization = authorizationInstance.get();
            Set<OpenShiftCluster> subscribedClusters = clusterRegistry.getSubscribedClusters(securityContext.getUserPrincipal());
            for (OpenShiftCluster cluster : subscribedClusters) {
                futures.add(identityProvider.getIdentityAsync(authorization, cluster.getId())
                                    .thenApply(
                                            identity -> identity.map(value -> getClusterVerified(cluster, value))
                                                    .orElseGet(() -> new ClusterVerified(cluster, false)))
                );
            }
            clusters = CompletableFutures.allAsList(futures).get();
        }
        return clusters;
    }

    private ClusterVerified getClusterVerified(OpenShiftCluster cluster, Identity identity) {
        final ImmutableParameters.Builder builder = ImmutableParameters.builder().cluster(cluster).identity(identity);
        final OpenShiftService service = openShiftServiceFactory.create(builder.build());
        try {
            service.getLoggedUser();
            return new ClusterVerified(cluster, true);
        } catch (KubernetesClientException e) {
            //means that we have an invalid token e.g. cluster got deprovisioned
            return new ClusterVerified(cluster, false);
        }
    }

    @GET
    @Path("/clusters/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<OpenShiftCluster> getAllOpenShiftClusters() {
        // Return all clusters
        return clusterRegistry.getClusters();
    }


    @HEAD
    @Secured
    @Path("/projects/{project}")
    public Response openShiftProjectExists(@NotNull @PathParam("project") String project) {
        if (openShiftService.get().projectExists(project)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Secured
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public OpenShiftUser getUser() {
        return openShiftService.get().getLoggedUser();
    }

    /**
     * Used in OpenShiftEndpoint#getSupportedOpenShiftClusters
     */
    private static class ClusterVerified {

        @JsonProperty
        final boolean connected;

        @JsonProperty
        final OpenShiftCluster cluster;

        private ClusterVerified(OpenShiftCluster cluster) {
            this.cluster = cluster;
            this.connected = true;
        }

        private ClusterVerified(OpenShiftCluster cluster, boolean connected) {
            this.cluster = cluster;
            this.connected = connected;
        }

    }

}