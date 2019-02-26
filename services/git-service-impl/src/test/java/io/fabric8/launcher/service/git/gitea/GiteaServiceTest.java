package io.fabric8.launcher.service.git.gitea;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.AbstractGitServiceTest;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment;
import io.fabric8.launcher.service.git.gitea.api.GiteaWebhookEvent;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GiteaServiceTest extends AbstractGitServiceTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("gitea.devtools-dev.ext.devshift.net");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly(GiteaEnvironment.LAUNCHER_BACKEND_GITEA_URL.propertyKey(), "http://gitea.devtools-dev.ext.devshift.net")
                               .andForSimulationOnly(GiteaEnvironment.LAUNCHER_BACKEND_GITEA_USERNAME.propertyKey(), "admin2")
                               .andForSimulationOnly(GiteaEnvironment.LAUNCHER_BACKEND_GITEA_TOKEN.propertyKey(), "6e11fb0f5e199972e38157e338d7e51bd6b4aff0"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    @Override
    protected GiteaServiceFactory getGitServiceFactory() {
        return new GiteaServiceFactory(HttpClient.create());
    }

    @Override
    protected GitServiceSpi getGitService() {
        GiteaServiceFactory factory = getGitServiceFactory();
        Identity identity = factory.getDefaultIdentity().orElseThrow(() -> new IllegalStateException("Default identity not found"));
        return (GitServiceSpi) factory.create(identity, "gastaldi");
    }

    @Override
    protected String[] getTestHookEvents() {
        return new String[]{GiteaWebhookEvent.PUSH.id(),
                GiteaWebhookEvent.PULL_REQUEST.id(),
                GiteaWebhookEvent.ISSUE_COMMENT.id()};
    }

    @Override
    protected String getTestLoggedUser() {
        return "gastaldi";
    }

    @Override
    protected ImmutableGitOrganization getTestOrganization() {
        return ImmutableGitOrganization.of("myorg");
    }

    @Override
    protected String getRawFileUrl(String fullRepoName, String fileName) {
        return "http://gitea.devtools-dev.ext.devshift.net/" + fullRepoName + "/raw/branch/master/" + fileName;
    }
}
