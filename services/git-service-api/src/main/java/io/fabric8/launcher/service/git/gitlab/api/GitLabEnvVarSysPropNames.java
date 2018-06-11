package io.fabric8.launcher.service.git.gitlab.api;

import io.fabric8.launcher.base.PropNamesSupport;

/**
 * Contains names of environment variables or system properties
 * relating to the GitLab Service
 */
public enum GitLabEnvVarSysPropNames implements PropNamesSupport {
    LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME,
    LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN,
    LAUNCHER_MISSIONCONTROL_GITLAB_URL
}