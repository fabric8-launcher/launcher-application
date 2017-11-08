package io.openshift.appdev.missioncontrol.service.github.test;


import io.openshift.appdev.missioncontrol.base.EnvironmentSupport;
import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.IdentityFactory;

/**
 * Used to obtain the GitHub credentials from the environment
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubTestCredentials {

    private GitHubTestCredentials() {
        // No instances
    }

    private static final String NAME_ENV_VAR_SYSPROP_LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME = "LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME";

    private static final String NAME_ENV_VAR_SYSPROP_LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN = "LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN";

    /**
     * @return the GitHub username
     */
    public static String getUsername() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME);
    }

    /**
     * @return the GitHub token
     */
    public static Identity getToken() {
        return IdentityFactory.createFromToken(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN));
    }
}
