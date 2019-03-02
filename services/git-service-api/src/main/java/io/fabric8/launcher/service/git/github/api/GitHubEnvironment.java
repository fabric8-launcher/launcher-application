package io.fabric8.launcher.service.git.github.api;

import io.fabric8.launcher.base.EnvironmentEnum;

/**
 * Contains names of environment variables or system properties
 * relating to the GitHub Service
 *
 * @deprecated use the {@link io.fabric8.launcher.service.git.api.GitServiceConfig} feature instead
 */
@Deprecated
public enum GitHubEnvironment implements EnvironmentEnum {
    LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME,
    LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN
}