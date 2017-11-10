package io.fabric8.launcher.service.openshift.impl;

import io.fabric8.launcher.service.openshift.api.OpenShiftService;

/**
 * A type that contains an {@link OpenShiftService}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftServiceContainer {

    /**
     * @return An {@link OpenShiftService}
     */
    OpenShiftService getOpenShiftService();
}
