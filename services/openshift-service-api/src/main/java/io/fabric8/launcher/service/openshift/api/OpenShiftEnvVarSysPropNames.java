package io.fabric8.launcher.service.openshift.api;

import io.fabric8.launcher.base.EnvironmentEnum;

/**
 * Contains names of environment variables or system properties
 * relating to the OpenShift Service
 */
public enum OpenShiftEnvVarSysPropNames implements EnvironmentEnum {

    LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL,
    LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL,
    LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE,
    /**
     * Used to query manage.openshift.com. See https://github.com/openshift/online-registration#subscription-lookup
     */
    LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_SUBSCRIPTION_TOKEN,
    LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME,
    LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD,
    LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN;

}
