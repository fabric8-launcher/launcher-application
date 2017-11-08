package io.openshift.appdev.missioncontrol.service.openshift.impl;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;

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
