package io.openshift.appdev.missioncontrol.service.openshift.api;

/**
 * Obtains the OpenShift URL according to precedence (lower number gets priority):
 * <p>
 * 1) System Property
 * 2) Environment Variable
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class OpenShiftSettings {

    /**
     * No instances
     */
    private OpenShiftSettings() {
    }

    /**
     * @return The URL the client should use to call the OpenShift API
     * from the environment variable or system properties value for
     * {@link OpenShiftEnvVarSysPropNames#OPENSHIFT_API_URL} or
     * {@code null} if no specific value was set.
     */
    public static String getOpenShiftApiUrl() {
        return getOpenShiftUrl(OpenShiftEnvVarSysPropNames.OPENSHIFT_API_URL);
    }

    /**
     * @return The URL the client should use to access the OpenShift Console
     * from the environment variable or system properties value for
     * {@link OpenShiftEnvVarSysPropNames#OPENSHIFT_CONSOLE_URL} or
     * {@code null} if no specific value was set.
     */
    public static String getOpenShiftConsoleUrl() {
        return getOpenShiftUrl(OpenShiftEnvVarSysPropNames.OPENSHIFT_CONSOLE_URL);
    }

    /**
     * The URL to the supported OpenShift configs
     *
     * @return The URL the client should use to call the OpenShift API
     * from the environment variable or system properties value for
     * {@link OpenShiftEnvVarSysPropNames#OPENSHIFT_CLUSTERS_CONFIG_FILE} or
     * {@code null} if no specific value was set.
     */
    public static String getOpenShiftClustersConfigFile() {
        return System.getProperty(OpenShiftEnvVarSysPropNames.OPENSHIFT_CLUSTERS_CONFIG_FILE,
                                  System.getenv(OpenShiftEnvVarSysPropNames.OPENSHIFT_CLUSTERS_CONFIG_FILE));
    }

    private static String getOpenShiftUrl(final String envVarOrSysPropName) {
        assert envVarOrSysPropName != null && !envVarOrSysPropName.isEmpty();
        if (isSystemPropertySet(envVarOrSysPropName)) {
            return System.getProperty(envVarOrSysPropName);
        } else if (isEnvVarSet(envVarOrSysPropName)) {
            return System.getenv(envVarOrSysPropName);
        }

        return null;
    }

    private static boolean isEnvVarSet(final String name) {
        String val = System.getenv(name);
        return val != null && !val.isEmpty();
    }

    private static boolean isSystemPropertySet(final String name) {
        String val = System.getProperty(name);
        return val != null && !val.isEmpty();
    }
}
