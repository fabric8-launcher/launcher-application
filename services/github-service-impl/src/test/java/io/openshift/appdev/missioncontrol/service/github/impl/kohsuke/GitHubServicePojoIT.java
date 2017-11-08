package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.test.GitHubTestCredentials;

/**
 * Unit Tests for the {@link GitHubService}
 * <p>
 * Relies on having environment variables set for: GITHUB_USERNAME GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class GitHubServicePojoIT extends GitHubServiceTestBase {

    @Override
    GitHubService getGitHubService() {
        return new KohsukeGitHubServiceFactoryImpl().create(GitHubTestCredentials.getToken());
    }

}
