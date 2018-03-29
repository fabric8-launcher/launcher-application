package io.fabric8.launcher.osio.client;

import java.util.Optional;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.api.OsioJenkinsClient;
import io.fabric8.launcher.osio.client.api.Tenant;
import io.fabric8.launcher.osio.client.impl.OsioJenkinsClientImpl;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OsioJenkinsClientTest {

    private static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("jenkins.openshift.io|jenkins.prod-preview.openshift.io");
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


    private OsioJenkinsClient getOsioJenkinsClient(){
        final IdentityProvider identityProvider = mock(IdentityProvider.class);
        when(identityProvider.getIdentity(Mockito.matches(IdentityProvider.ServiceType.GITHUB)))
                .thenReturn(Optional.of(createFromToken("dummy git token")));
        return  new OsioJenkinsClientImpl(createRequestAuthorizationHeaderValue(getOsioIdentity()), identityProvider);
    }

    @Test
    public void shouldEnsureCredentials() {
        final IdentityProvider identityProvider = mock(IdentityProvider.class);
        when(identityProvider.getIdentity(Mockito.matches(IdentityProvider.ServiceType.GITHUB)))
                .thenReturn(Optional.of(createFromToken("dummy git token")));
        final Tenant tenant = mock(Tenant.class);
        when(tenant.getIdentity())
                .thenReturn(createFromToken("authorization bearer token"));
        getOsioJenkinsClient().ensureCredentials("edewit");
    }

    private static TokenIdentity getOsioIdentity() {
        return createFromToken(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

}
