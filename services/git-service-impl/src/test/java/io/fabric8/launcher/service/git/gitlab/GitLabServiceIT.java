package io.fabric8.launcher.service.git.gitlab;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.AbstractGitServiceIT;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.git.gitlab.api.GitLabWebhookEvent;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.service.git.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN;
import static io.fabric8.launcher.service.git.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME;

public class GitLabServiceIT extends AbstractGitServiceIT {

    @ClassRule
    public static RuleChain RULE_CHAIN = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment()
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME, "fabric8-launcher")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN, "aefeajfnUZ3332"));

    @Rule
    public LauncherPerTestHoverflyRule hoverflyRule = new LauncherPerTestHoverflyRule("gitlab.com");

    private GitLabService gitLabService = new GitLabServiceFactory().create();

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
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME);
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
