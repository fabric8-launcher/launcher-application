package io.fabric8.launcher.web.endpoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.spotify.futures.CompletableFutures;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.ImmutableParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftUser;

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
    public Collection<ClusterVerified> getSupportedOpenShiftClusters() throws ExecutionException, InterruptedException {
        final Collection<ClusterVerified> clusters;
        if (!openShiftServiceFactory.getDefaultIdentity().isPresent()) {
            final List<CompletableFuture<ClusterVerified>> futures = new ArrayList<>();
            IdentityProvider identityProvider = identityProviderInstance.get();
            TokenIdentity authorization = authorizationInstance.get();
            Set<OpenShiftCluster> subscribedClusters = clusterRegistry.getSubscribedClusters(securityContext.getUserPrincipal());
            for (OpenShiftCluster cluster : subscribedClusters) {
                futures.add(identityProvider.getIdentityAsync(authorization, cluster.getId())
                                    .thenApply(identity -> {
                                        if (!identity.isPresent()) {
                                            return new ClusterVerified(cluster, false);
                                        }
                                        try {
                                            // Identity is set. Try to connect to cluster to check if it's still valid
                                            openShiftServiceFactory.create(ImmutableParameters.builder()
                                                                                   .cluster(cluster)
                                                                                   .identity(identity.get())
                                                                                   .build());
                                            return new ClusterVerified(cluster, true);
                                        } catch (KubernetesClientException e) {
                                            //means that we have an invalid token e.g. cluster got deprovisioned
                                            return new ClusterVerified(cluster, false);
                                        }
                                    }));
            }
            clusters = CompletableFutures.allAsList(futures).get();
        } else {
            clusters = clusterRegistry.getClusters().stream()
                    .map(ClusterVerified::new)
                    .collect(Collectors.toList());
        }
        return clusters;
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

        private ClusterVerified(OpenShiftCluster cluster) {
            this.connected = true;
            this.cluster = cluster;
        }

        private ClusterVerified(OpenShiftCluster cluster, boolean connected) {
            this.connected = connected;
            this.cluster = cluster;
        }

        private final boolean connected;

        private final OpenShiftCluster cluster;

        public OpenShiftCluster getCluster() {
            return cluster;
        }

        public boolean isConnected() {
            return connected;
        }
    }

}