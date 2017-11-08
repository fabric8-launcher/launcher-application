package io.openshift.appdev.missioncontrol.service.openshift.api;

/**
 * Contains names of environment variables or system properties
 * relating to the OpenShift Service
 */
public interface OpenShiftEnvVarSysPropNames {

    String OPENSHIFT_API_URL = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_API_URL";

    String OPENSHIFT_CONSOLE_URL = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL";

    /**
     * A YAML file containing the supported openshift clusters
     */
    String OPENSHIFT_CLUSTERS_CONFIG_FILE = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE";

}
