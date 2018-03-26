package io.fabric8.launcher.osio.client;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createHoverflyProxy;

public class OsioApiClientTest {

    private static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    private static final HoverflyRule HOVERFLY_RULE = createHoverflyProxy("wit-simulation.json",
                                                                          "api.openshift.io|api.prod-preview.openshift.io");
    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE))
            .around(HOVERFLY_RULE);

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();


    private OsioApiClient getOsioApiClient(){
        return  new OsioApiClientImpl(getOsioIdentity());
    }

    @Test
    public void readTenantData() {
        Tenant tenant = getOsioApiClient().getTenant();
        softly.assertThat(tenant.getUsername()).isEqualTo("foo");
        softly.assertThat(tenant.getEmail()).isEqualTo("foo@example.com");
        softly.assertThat(tenant.getNamespaces().size()).isEqualTo(5);
    }

    private static TokenIdentity getOsioIdentity() {
        if(LauncherHoverflyRuleConfigurer.isHoverflyInSimulationMode()){
            return IdentityFactory.createFromToken("jneoufze937973HFRH");
        }
        return IdentityFactory.createFromToken(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

}
