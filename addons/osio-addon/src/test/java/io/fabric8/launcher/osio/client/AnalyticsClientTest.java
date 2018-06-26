package io.fabric8.launcher.osio.client;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import okhttp3.MediaType;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;

public class AnalyticsClientTest {
    private static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("recommender.api.prod-preview.openshift.io|recommender.api.openshift.io");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                    .andForSimulationOnly(LAUNCHER_OSIO_TOKEN, "jneoufze937973HFRH"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);


    private AnalyticsClient getAnalyticsClient() {
        return new AnalyticsClient(OsioTests.getTestAuthorization(), HttpClient.create());
    }

    @Test
    public void should_run_depeditor_cve_analyses() {
        AnalyticsClient analyticsClient = getAnalyticsClient();
        final MediaType CONTENT_TYPE = MediaType.parse("application/json");
        final String payload = "{ \"request_id\": \"d2f5044f2b8740e3804261f5f864c11d\", \"_resolved\": [ { \"package\": \"io.vertx:vertx-core\", \"version\": \"3.5.0\" }], \"ecosystem\": \"maven\" }";
        analyticsClient.Request("/api/v1/depeditor-cve-analyses", CONTENT_TYPE, payload);
    }

}
