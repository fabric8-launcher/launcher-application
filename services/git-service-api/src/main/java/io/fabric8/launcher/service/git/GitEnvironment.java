package io.fabric8.launcher.service.git;

import io.fabric8.launcher.base.EnvironmentEnum;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public enum GitEnvironment implements EnvironmentEnum {
    LAUNCHER_GIT_PROVIDER,
    LAUNCHER_GIT_COMMITTER_AUTHOR,
    LAUNCHER_GIT_COMMITTER_AUTHOR_EMAIL
}
