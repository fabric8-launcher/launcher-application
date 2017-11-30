package io.fabric8.launcher.core.api;

import io.fabric8.launcher.base.identity.Identity;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Identities {
    Identity getGitHubIdentity(String authorization);

    Identity getOpenShiftIdentity(String authorization, String cluster);

}
