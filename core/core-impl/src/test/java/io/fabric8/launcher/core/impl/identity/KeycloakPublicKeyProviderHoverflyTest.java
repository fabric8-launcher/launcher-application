package io.fabric8.launcher.core.impl.identity;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.RSAPublicKeyConverter;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.base.test.identity.TokenFixtures;
import io.fabric8.launcher.core.spi.PublicKeyProvider;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.EnvironmentSupport.getRequiredEnvVarOrSysProp;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_KEYCLOAK_REALM;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_KEYCLOAK_URL;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.KID;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.STRIP_PUBLIC_KEY;
import static org.assertj.core.api.Assertions.assertThat;

public class KeycloakPublicKeyProviderHoverflyTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("sso.openshift.io");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly("KEYCLOAK_TOKEN", TokenFixtures.VALID_TOKEN)
                               .andForSimulationOnly(LAUNCHER_KEYCLOAK_URL.propertyKey(), "https://sso.openshift.io/auth")
                               .andForSimulationOnly(LAUNCHER_KEYCLOAK_REALM.propertyKey(), "rh-developers-launch"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    private final PublicKeyProvider publicKeyProvider =
            new KeycloakPublicKeyProvider(ImmutableKeycloakParameters.builder().build(), getKeycloakToken(), HttpClient.create());

    @Test
    public void should_receive_key_based_on_its_kid() {
        // when
        final Optional<RSAPublicKey> publicKey = publicKeyProvider.getKey(KID);
        // then
        assertThat(publicKey).isPresent()
                .get()
                .isEqualTo(RSAPublicKeyConverter.fromString(STRIP_PUBLIC_KEY));
    }

    @Test
    public void should_not_find_public_key_for_non_existing_kid() {
        // when
        final Optional<RSAPublicKey> publicKey = publicKeyProvider.getKey("non-existing-key");

        // then
        assertThat(publicKey).isNotPresent();
    }

    @Test
    public void should_not_find_public_key_for_key_with_non_public_key_defined_in_response() {
        // when
        final Optional<RSAPublicKey> publicKey = publicKeyProvider.getKey("856296dd-4169-4cea-aeba-96265d7a556c");

        // then
        assertThat(publicKey).isNotPresent();
    }

    @Test
    public void should_return_empty_response_when_error_occurs() {
        // when
        final Optional<RSAPublicKey> publicKey = publicKeyProvider.getKey("856296dd-4169-4cea-aeba-96265d7a556c");

        // then
        assertThat(publicKey).isNotPresent();
    }


    private static TokenIdentity getKeycloakToken() {
        return TokenIdentity.of(getRequiredEnvVarOrSysProp("KEYCLOAK_TOKEN"));
    }
}