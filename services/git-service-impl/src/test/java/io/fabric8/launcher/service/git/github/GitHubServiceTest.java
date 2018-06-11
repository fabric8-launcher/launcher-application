package io.fabric8.launcher.service.git.github;

import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.AbstractGitServiceTest;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.github.api.GitHubWebhookEvent;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.service.git.github.api.GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN;
import static io.fabric8.launcher.service.git.github.api.GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME;


/**
 * Integration Tests for the {@link GitService}
 */
public class GitHubServiceTest extends AbstractGitServiceTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("github.com|githubusercontent.com");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME.propertyKey(), "gastaldi-osio")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN.propertyKey(), "nefjnFEJNKJEA73793"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

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
        return LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME.value();
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
