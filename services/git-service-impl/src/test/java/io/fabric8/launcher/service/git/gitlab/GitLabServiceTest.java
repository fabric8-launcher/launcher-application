package io.fabric8.launcher.service.git.gitlab;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.AbstractGitServiceTest;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.gitlab.api.GitLabWebhookEvent;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.service.git.gitlab.api.GitLabEnvironment.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN;
import static io.fabric8.launcher.service.git.gitlab.api.GitLabEnvironment.LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME;

public class GitLabServiceTest extends AbstractGitServiceTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("gitlab.com");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME.propertyKey(), "fabric8-launcher")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN.propertyKey(), "aefeajfnUZ3332"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    private GitLabService gitLabService = new GitLabServiceFactory(HttpClient.create()).create();

    @Override
    protected GitServiceSpi getGitService() {
        return gitLabService;
    }

    @Override
    protected String[] getTestHookEvents() {
        return new String[]{GitLabWebhookEvent.PUSH.id(), GitLabWebhookEvent.MERGE_REQUESTS.id()};
    }

    @Override
    protected String getTestLoggedUser() {
        return LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME.value();
    }

    @Override
    protected ImmutableGitOrganization getTestOrganization() {
        return ImmutableGitOrganization.of("fabric8-launcher-it");
    }

    @Override
    protected String getRawFileUrl(String fullRepoName, String fileName) {
        return "https://gitlab.com/" + fullRepoName + "/raw/master/" + fileName;
    }

}
