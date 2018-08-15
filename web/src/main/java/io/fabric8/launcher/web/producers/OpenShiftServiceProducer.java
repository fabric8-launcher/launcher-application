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
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static java.util.Objects.requireNonNull;

/**
 * Produces {@link OpenShiftService} instances per-request
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class OpenShiftServiceProducer {

    private static final String OPENSHIFT_CLUSTER_PARAMETER = "X-OpenShift-Cluster";

    private static final boolean IMPERSONATE_USER = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_IMPERSONATE_USER.booleanValue();

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
        ImmutableOpenShiftParameters.Builder builder = ImmutableOpenShiftParameters.builder().cluster(cluster)
                .identity(identity);
        if (IMPERSONATE_USER) {
            // See SecuredFilter#filter
            String userPrincipal = (String) request.getAttribute("USER_NAME");
            builder.impersonateUsername(requireNonNull(userPrincipal, "User name is required"));
        }
        return factory.create(builder.build());
    }

}
