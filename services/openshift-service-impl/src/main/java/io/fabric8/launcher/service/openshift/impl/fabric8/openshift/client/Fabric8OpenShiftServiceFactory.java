package io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * {@link OpenShiftServiceFactory} implementation
 *
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@ApplicationScoped
public class Fabric8OpenShiftServiceFactory implements OpenShiftServiceFactory {

    /**
     * Needed for proxying
     */
    private Fabric8OpenShiftServiceFactory() {
        this.clusterRegistry = null;
    }

    @Inject
    public Fabric8OpenShiftServiceFactory(OpenShiftClusterRegistry clusterRegistry) {
        this.clusterRegistry = clusterRegistry;
    }

    private final OpenShiftClusterRegistry clusterRegistry;

    private Logger log = Logger.getLogger(Fabric8OpenShiftServiceFactory.class.getName());

    /**
     * Creates a new {@link OpenShiftService} with the specified credentials
     *
     * @param identity the credentials to use for this {@link OpenShiftService}
     * @return the created {@link OpenShiftService}
     * @throws IllegalArgumentException If the {@code identity} is not specified
     */
    @Override
    public Fabric8OpenShiftServiceImpl create(Identity identity) {
        return create(clusterRegistry.getDefault(), identity);
    }

    /**
     * Creates a new {@link OpenShiftService} with the specified, required urls and oauthToken
     *
     * @param identity the credentials to use for this {@link OpenShiftService
     * @return the created {@link OpenShiftService}
     * @throws IllegalArgumentException If the {@code identity} is not specified
     */
    @Override
    public Fabric8OpenShiftServiceImpl create(final OpenShiftCluster openShiftCluster, final Identity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("identity is required");
        }
        assert openShiftCluster != null : "OpenShiftCluster is required";
        // Create and return
        log.finest(() -> "Created backing OpenShift client for " + openShiftCluster.getApiUrl());
        return new Fabric8OpenShiftServiceImpl(openShiftCluster.getApiUrl(), openShiftCluster.getConsoleUrl(), identity);
    }
}
