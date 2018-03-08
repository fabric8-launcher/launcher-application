package io.fabric8.launcher.service.github;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.AbstractGitServiceIT;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.github.api.GitHubWebhookEvent;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.service.github.api.GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN;
import static io.fabric8.launcher.service.github.api.GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME;


/**
 * Integration Tests for the {@link GitService}
 */
public class GitHubServiceIT extends AbstractGitServiceIT {

    @ClassRule
    public static RuleChain RULE_CHAIN = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment()
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME, "gastaldi-osio")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN, "nefjnFEJNKJEA73793"));

    @Rule
    public LauncherPerTestHoverflyRule hoverflyRule = new LauncherPerTestHoverflyRule("github.com|githubusercontent.com");

    @Override
    protected GitServiceSpi getGitService() {
        return (GitServiceSpi) new KohsukeGitHubServiceFactory().create();
    }

    @Override
    protected String[] getTestHookEvents() {
        return new String[]{GitHubWebhookEvent.PUSH.id(), GitHubWebhookEvent.PULL_REQUEST.id()};
    }

    @Override
    protected String getTestLoggedUser() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME);
    }

    @Override
    protected ImmutableGitOrganization getTestOrganization() {
        return ImmutableGitOrganization.of("fabric8-launcher-it");
    }

    @Override
    protected String getRawFileUrl(String fullRepoName, String fileName) {
        return "https://raw.githubusercontent.com/" + fullRepoName + "/master/" + fileName;
    }

}
