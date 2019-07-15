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
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.ImmutableParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftEnvironment;
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

    /**
     * If provided, lookup in the {@link OpenShiftClusterRegistry}. It is ignored if OPENSHIFT_CLUSTER_URL_HEADER is set
     */
    private static final String OPENSHIFT_CLUSTER_HEADER = "X-OpenShift-Cluster";

    /**
     * Use this OpenShift API URL instead of the predefined ones
     */
    static final String OPENSHIFT_CLUSTER_URL_HEADER = "X-OpenShift-Cluster-URL";

    /**
     * The header containing the bearer token used for authenticating against the requested OpenShift service
     */
    public static final String OPENSHIFT_AUTHORIZATION_HEADER = "X-OpenShift-Authorization";

    private static final boolean IMPERSONATE_USER = OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_IMPERSONATE_USER.booleanValue();

    private final OpenShiftServiceFactory factory;

    private final OpenShiftClusterRegistry clusterRegistry;

    /**
     * For proxy purposes only
     */
    @Deprecated
    OpenShiftServiceProducer() {
        this.factory = null;
        this.clusterRegistry = null;
    }

    @Inject
    OpenShiftServiceProducer(OpenShiftServiceFactory factory, OpenShiftClusterRegistry clusterRegistry) {
        this.factory = factory;
        this.clusterRegistry = clusterRegistry;
    }

    @RequestScoped
    @Produces
    OpenShiftService getOpenShiftService(final HttpServletRequest request,
                                         final IdentityProvider identityProvider,
                                         final TokenIdentity authorization) {
        final OpenShiftCluster cluster;
        String apiUrl = request.getHeader(OPENSHIFT_CLUSTER_URL_HEADER);
        final String clusterId = Objects.toString(request.getHeader(OPENSHIFT_CLUSTER_HEADER), IdentityProvider.ServiceType.OPENSHIFT);
        if (apiUrl != null) {
            cluster = ImmutableOpenShiftCluster.builder().id("custom").apiUrl(apiUrl).build();
        } else {
            // Launcher authenticates in different clusters
            cluster = clusterRegistry.findClusterById(clusterId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid OpenShift Cluster: " + clusterId));
        }
        String openShiftAuth = request.getHeader(OPENSHIFT_AUTHORIZATION_HEADER);
        final Identity identity;
        if (openShiftAuth != null) {
            // Supports Bearer Authorization headers only
            identity = TokenIdentity.fromBearerAuthorizationHeader(openShiftAuth);
        } else {
            identity = factory.getDefaultIdentity().orElseGet(() -> identityProvider.getIdentity(authorization, clusterId)
                    .orElseThrow(() -> new NotFoundException("OpenShift identity not found")));
        }
        ImmutableParameters.Builder builder = ImmutableParameters.builder().cluster(cluster).identity(identity);
        if (IMPERSONATE_USER) {
            // See SecuredFilter#filter
            String userPrincipal = (String) request.getAttribute("USER_NAME");
            builder.impersonateUsername(requireNonNull(userPrincipal, "User name is required"));
        }
        return factory.create(builder.build());
    }

}
