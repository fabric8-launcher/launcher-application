package io.fabric8.launcher.osio.client;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.RSAPublicKeyConverter;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.core.spi.PublicKeyProvider;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.KID;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.STRIP_PUBLIC_KEY;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.VALID_TOKEN;
import static io.fabric8.launcher.osio.client.OsioTests.LAUNCHER_OSIO_TOKEN;
import static io.fabric8.launcher.osio.client.OsioTests.getTestAuthorization;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthPublicKeyProviderTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("auth.openshift.io|auth.prod-preview.openshift.io");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly(LAUNCHER_OSIO_TOKEN, VALID_TOKEN))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    @Test
    public void should_receive_key_based_on_its_kid() {
        // given
        final PublicKeyProvider publicKeyProvider = new AuthPublicKeyProvider(getTestAuthorization(), HttpClient.create());

        // when
        final Optional<RSAPublicKey> publicKey = publicKeyProvider.getKey(KID);

        // then
        assertThat(publicKey).isPresent()
                .get()
                .isEqualTo(RSAPublicKeyConverter.fromString(STRIP_PUBLIC_KEY));
    }

}
