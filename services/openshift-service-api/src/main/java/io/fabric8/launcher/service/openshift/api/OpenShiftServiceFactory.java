package io.fabric8.launcher.service.openshift.api;


import java.util.Optional;

import javax.annotation.Nullable;

import io.fabric8.launcher.base.identity.Identity;
import org.immutables.value.Value;

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
     * Returns an {@link OpenShiftService} given its {@link Parameters}
     *
     * @param parameters an identity
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(Parameters parameters);

    /**
     * Returns the default identity for the OpenShift service
     *
     * @return an optional {@link Identity} if set
     */
    Optional<Identity> getDefaultIdentity();

    /**
     * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
     */
    @Value.Immutable
    interface Parameters {

        OpenShiftCluster getCluster();

        Identity getIdentity();

        @Nullable
        String getImpersonateUsername();
    }
}
