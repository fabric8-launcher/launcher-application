package io.fabric8.launcher.service.git.bitbucket.api;

import io.fabric8.launcher.base.EnvironmentEnum;

/**
 * Contains names of environment variables or system properties
 * relating to the Bitbucket Service
 *
 * @deprecated use the {@link io.fabric8.launcher.service.git.api.GitServiceConfig} feature instead
 */
@Deprecated
public enum BitbucketEnvironment implements EnvironmentEnum {
    LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME,
    LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD
}