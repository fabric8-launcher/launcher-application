package io.fabric8.launcher.web.producers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class OpenShiftServiceProducer {

    @Inject
    private OpenShiftServiceFactory factory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @RequestScoped
    @Produces
    OpenShiftService getOpenShiftService(HttpServletRequest request, IdentityProvider identityProvider) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String clusterId = Objects.toString(request.getParameter("clusterId"), IdentityProvider.ServiceType.OPENSHIFT);
        // Launcher authenticates in different clusters
        OpenShiftCluster cluster = clusterRegistry.findClusterById(clusterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OpenShift Cluster"));

        Identity identity = factory.getDefaultIdentity()
                .orElseGet(() -> identityProvider.getIdentity(clusterId, authorization)
                        .orElseThrow(() -> new NotFoundException("OpenShift identity not defined")));
        return factory.create(cluster, identity);
    }

}
