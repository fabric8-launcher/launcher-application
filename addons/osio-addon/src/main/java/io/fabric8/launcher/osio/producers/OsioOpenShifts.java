package io.fabric8.launcher.osio.producers;

import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;

public final class OsioOpenShifts {

    private OsioOpenShifts() {
        throw new IllegalAccessError("Helper class");
    }

    public static final OpenShiftCluster OSIO_CLUSTER = new OpenShiftCluster("osio",
                                                                             "osio",
                                                                             EnvironmentVariables.getOpenShiftApiURL(),
                                                                             EnvironmentVariables.getOpenShiftApiURL());
}
