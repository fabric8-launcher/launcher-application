package io.fabric8.launcher.core.api.projectiles.context;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitCapable {
    @Nullable
    String getGitOrganization();

    String getGitRepository();
}
