package io.fabric8.launcher.web.producers;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * Produces {@link OpenShiftService} instances per-request
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class OpenShiftServiceProducer {

    private static final String OPENSHIFT_CLUSTER_PARAMETER = "X-OpenShift-Cluster";

    @Inject
    private OpenShiftServiceFactory factory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @RequestScoped
    @Produces
    OpenShiftService getOpenShiftService(final HttpServletRequest request, final IdentityProvider identityProvider, final TokenIdentity authorization) {
        final String clusterId = Objects.toString(request.getHeader(OPENSHIFT_CLUSTER_PARAMETER), IdentityProvider.ServiceType.OPENSHIFT);
        // Launcher authenticates in different clusters
        final OpenShiftCluster cluster = clusterRegistry.findClusterById(clusterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OpenShift Cluster: " + clusterId));
        final Identity identity = factory.getDefaultIdentity().orElseGet(() -> identityProvider.getIdentity(authorization, clusterId)
                .orElseThrow(() -> new NotFoundException("OpenShift identity not found")));
        return factory.create(cluster, identity);
    }

}
