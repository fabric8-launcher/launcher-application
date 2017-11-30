package io.fabric8.launcher.core.api;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;

/**
 * Provides {@link Identity} objects for the main services
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Identities {

    /**
     * Returns the GitHub identity based on the given authorization token
     *
     * @param authorization token
     * @return default identity or the one authenticated through the given authorization token
     */
    Identity getGitHubIdentity(String authorization);

    /**
     * Returns the OpenShift identity based on the given authorization token
     *
     * @param authorization token
     * @param cluster       the cluster this authorization belongs to.
     *                      May be null, in this case it will use the default cluster returned in {@link OpenShiftClusterRegistry#getDefault()}
     * @return default identity or the one authenticated through the given authorization token
     */
    Identity getOpenShiftIdentity(String authorization, String cluster);

}
