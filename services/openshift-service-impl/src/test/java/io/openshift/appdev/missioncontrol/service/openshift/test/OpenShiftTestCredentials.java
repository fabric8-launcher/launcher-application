package io.openshift.appdev.missioncontrol.service.openshift.test;


import io.openshift.appdev.missioncontrol.base.EnvironmentSupport;
import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.IdentityFactory;

/**
 * Used to obtain the OpenShift credentials from the environment
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftTestCredentials {

    private OpenShiftTestCredentials() {
        // No instances
    }

    private static final String NAME_ENV_VAR_SYSPROP_OPENSHIFT_USERNAME = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME";
    private static final String NAME_ENV_VAR_SYSPROP_OPENSHIFT_PASSWORD = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD";

    /**
     * @return the Openshift identity
     */
    public static Identity getIdentity() {
        return IdentityFactory.createFromUserPassword(
                EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_OPENSHIFT_USERNAME),
                EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_OPENSHIFT_PASSWORD));
    }
}
