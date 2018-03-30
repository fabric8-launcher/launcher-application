package io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.ImmutableUserPasswordIdentity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import io.fabric8.utils.Strings;

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


    @Override
    public Optional<Identity> getDefaultIdentity() {
        if (!isDefaultIdentitySet()) {
            return Optional.empty();
        }
        // Read from the ENV variables
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN);
        if (Strings.isNotBlank(token)) {
            return Optional.of(TokenIdentity.of(token));
        } else {
            String user = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME);
            String password = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD);
            return Optional.of(ImmutableUserPasswordIdentity.of(user, password));
        }
    }

    private boolean isDefaultIdentitySet() {
        String user = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME);
        String password = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD);
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN);

        return ((user != null && password != null) || token != null);
    }
}