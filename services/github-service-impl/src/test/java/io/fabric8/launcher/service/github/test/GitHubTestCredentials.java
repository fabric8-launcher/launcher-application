package io.fabric8.launcher.service.github.test;


import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.github.api.GitHubEnvVarSysPropNames;

/**
 * Used to obtain the GitHub credentials from the environment
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubTestCredentials {

    private GitHubTestCredentials() {
        // No instances
    }

    /**
     * @return the GitHub username
     */
    public static String getUsername() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(GitHubEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME);
    }

    /**
     * @return the GitHub token
     */
    public static Identity getToken() {
        return IdentityFactory.createFromToken(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(GitHubEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN));
    }
}
