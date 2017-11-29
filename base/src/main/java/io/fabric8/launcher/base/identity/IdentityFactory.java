package io.fabric8.launcher.base.identity;

import io.fabric8.launcher.base.EnvironmentSupport;

/**
 * Creates {@link Identity} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactory {
    static final String LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME";
    static final String LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD";
    static final String LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN";
    private static final String LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN = "LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN";

    private IdentityFactory() {
    }

    public static TokenIdentity createFromToken(String token) {
        return new TokenIdentity(token);
    }

    public static UserPasswordIdentity createFromUserPassword(String user, String password) {
        return new UserPasswordIdentity(user, password);
    }

    public static boolean useDefaultIdentities() {
        String user = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME);
        String password = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD);
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN);

        return ((user != null && password != null) || token != null);
    }

    public static Identity getDefaultOpenShiftIdentity() {
        // Read from the ENV variables
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(IdentityFactory.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN);
        if (token != null) {
            return IdentityFactory.createFromToken(token);
        } else {
            String user = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(IdentityFactory.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME);
            String password = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(IdentityFactory.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD);
            return IdentityFactory.createFromUserPassword(user, password);
        }
    }

    public static Identity getDefaultGithubIdentity() {
        // Try using the provided Github token
        String token = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(IdentityFactory.LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN);
        return IdentityFactory.createFromToken(token);
    }

}
