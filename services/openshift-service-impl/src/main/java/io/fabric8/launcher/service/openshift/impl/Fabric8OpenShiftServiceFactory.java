package io.fabric8.launcher.service.openshift.impl;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.ImmutableUserPasswordIdentity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD;
import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN;
import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME;

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


    @Override
    public OpenShiftService create() {
        return create(getDefaultIdentity().
                orElseThrow(() -> new IllegalStateException("OpenShift Credentials not found. Are the required environment variables set?")));
    }

    /**
     * Creates a new {@link OpenShiftService} with the specified credentials
     *
     * @param identity the credentials to use for this {@link OpenShiftService}
     * @return the created {@link OpenShiftService}
     * @throws IllegalArgumentException If the {@code identity} is not specified
     */
    @Override
    public Fabric8OpenShiftServiceImpl create(Identity identity) {
        OpenShiftParameters parameters = ImmutableOpenShiftParameters.builder()
                .cluster(clusterRegistry.getDefault())
                .identity(identity)
                .build();
        return create(parameters);
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
        OpenShiftParameters parameters = ImmutableOpenShiftParameters.builder()
                .cluster(openShiftCluster)
                .identity(identity)
                .build();

        return create(parameters);
    }

    @Override
    public Fabric8OpenShiftServiceImpl create(OpenShiftParameters parameters) {
        return new Fabric8OpenShiftServiceImpl(parameters);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        if (!isDefaultIdentitySet()) {
            return Optional.empty();
        }
        // Read from the ENV variables
        String token = LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.value();
        if (token != null) {
            return Optional.of(TokenIdentity.of(token));
        } else {
            String user = LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.valueRequired();
            String password = LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.valueRequired();
            return Optional.of(ImmutableUserPasswordIdentity.of(user, password));
        }
    }

    private boolean isDefaultIdentitySet() {
        String user = LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.value();
        String password = LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.value();
        String token = LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.value();

        return ((user != null && password != null) || token != null);
    }
}