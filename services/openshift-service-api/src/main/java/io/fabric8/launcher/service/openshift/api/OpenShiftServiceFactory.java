package io.fabric8.launcher.service.openshift.api;


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
     * Checks if the default identity for this service is set
     *
     * @return true if the default OpenShift identity is set
     */
    boolean isDefaultIdentitySet();
}
