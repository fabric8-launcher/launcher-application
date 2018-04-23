package io.fabric8.launcher.osio.client;

import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.osio.client.OsioTests.LAUNCHER_OSIO_TOKEN;
import static io.fabric8.launcher.osio.client.OsioTests.getTestAuthorization;

public class OsioIdentityProviderTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("auth.openshift.io|auth.prod-preview.openshift.io");

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


    private OsioIdentityProvider getOsioAuthClient() {
        return new OsioIdentityProvider(HttpClient.create());
    }

    @Test
    public void shouldProvideIdentityCorrectly() throws Exception {
        final Optional<Identity> gitIdentity = getOsioAuthClient().getIdentityAsync(getTestAuthorization(), IdentityProvider.ServiceType.GITHUB).get();
        Assertions.assertThat(gitIdentity)
                .isPresent().get()
                .isEqualTo(TokenIdentity.of("20234c2a7c51348cad0aa4fb853e7c65957b79b4"));
    }

}
