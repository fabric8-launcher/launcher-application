package io.fabric8.launcher.service.git.bitbucket;


import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.AbstractGitServiceTest;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.bitbucket.api.BitbucketWebhookEvent;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;

public class BitbucketServiceTest extends AbstractGitServiceTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("bitbucket.org");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME, "fabric8-launcher")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD, "enfjaj2RE3R3JNF"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);


    private GitServiceSpi gitServiceSpi = (GitServiceSpi) new BitbucketServiceFactory().create();

    @Override
    protected GitServiceSpi getGitService() {
        return gitServiceSpi;
    }

    @Override
    protected String[] getTestHookEvents() {
        return new String[]{BitbucketWebhookEvent.REPO_PUSH.id(), BitbucketWebhookEvent.PULL_REQUEST_CREATED.id()};
    }

    @Override
    protected String getTestLoggedUser() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME);
    }

    @Override
    protected ImmutableGitOrganization getTestOrganization() {
        return ImmutableGitOrganization.of("fabric8-launcher-it");
    }

    @Override
    protected String getRawFileUrl(String fullRepoName, String fileName) {
        return "https://bitbucket.org/" + fullRepoName + "/raw/master/" + fileName;
    }
}
