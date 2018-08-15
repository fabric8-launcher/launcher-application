package io.fabric8.launcher.service.openshift.api;


import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;

/**
 * Creates {@link OpenShiftService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OpenShiftServiceFactory {

    /**
     * Returns an {@link OpenShiftService} using the default authentication in the default cluster
     *
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create();

    /**
     * Returns an {@link OpenShiftService} given it's OAuth token in the default cluster
     *
     * @param identity an identity
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(Identity identity);

    /**
     * Returns an {@link OpenShiftService} given it's {@link OpenShiftCluster} and OAuth token
     *
     * @param identity an identity
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(OpenShiftCluster cluster, Identity identity);

    /**
     * Returns an {@link OpenShiftService} given it's {@link OpenShiftParameters}
     *
     * @param parameters an identity
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(OpenShiftParameters parameters);

    /**
     * Returns the default identity for the OpenShift service
     *
     * @return an optional {@link Identity} if set
     */
    Optional<Identity> getDefaultIdentity();

}
