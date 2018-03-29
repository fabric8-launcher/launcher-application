package io.fabric8.launcher.osio.client;

import java.util.List;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.osio.client.api.OsioWitClient;
import io.fabric8.launcher.osio.client.api.Tenant;
import io.fabric8.launcher.osio.client.impl.OsioWitClientImpl;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;

public class OsioWitClientTest {

    private static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("api.openshift.io|api.prod-preview.openshift.io");
    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                .andForSimulationOnly(LAUNCHER_OSIO_TOKEN, "jneoufze937973HFRH"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);


    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();


    private OsioWitClient getOsioWitClient(){
        return  new OsioWitClientImpl(createRequestAuthorizationHeaderValue(getOsioIdentity()));
    }

    @Test
    public void shouldGetTenantCorrectly() {
        final Tenant tenant = getOsioWitClient().getTenant();
        final Tenant.UserInfo userInfo = tenant.getUserInfo();
        final List<Tenant.Namespace> namespaces = tenant.getNamespaces();
        softly.assertThat(userInfo.getUsername()).isEqualTo("foo");
        softly.assertThat(userInfo.getEmail()).isEqualTo("foo@example.com");
        softly.assertThat(namespaces).hasSize(5);
    }

    private static TokenIdentity getOsioIdentity() {
        return createFromToken(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

}
