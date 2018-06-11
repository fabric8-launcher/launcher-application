package io.fabric8.launcher.core.impl.identity;

import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.EnvironmentSupport.getRequiredEnvVarOrSysProp;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.core.impl.identity.KeycloakIdentityProvider.LAUNCHER_KEYCLOAK_REALM;
import static io.fabric8.launcher.core.impl.identity.KeycloakIdentityProvider.LAUNCHER_KEYCLOAK_URL;

public class KeycloakIdentityProviderHoverflyTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("sso.openshift.io");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly("KEYCLOAK_TOKEN", "efbheifb279272FUHUEHFHUEHF")
                               .andForSimulationOnly(LAUNCHER_KEYCLOAK_URL, "https://sso.openshift.io/auth")
                               .andForSimulationOnly(LAUNCHER_KEYCLOAK_REALM, "rh-developers-launch"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void should_get_github_token_correctly() throws Exception {
        final IdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider(HttpClient.create());

        final Optional<Identity> gitHubIdentityAsync = keycloakIdentityProvider.getIdentityAsync(getKeycloakToken(), "github").get();
        softly.assertThat(gitHubIdentityAsync)
                .isPresent()
                .get()
                .isInstanceOf(TokenIdentity.class);

        final Optional<Identity> gitHubIdentity = keycloakIdentityProvider.getIdentity(getKeycloakToken(), "github");
        softly.assertThat(gitHubIdentity)
                .isPresent()
                .get()
                .isInstanceOf(TokenIdentity.class);
    }

    @Test
    public void should_get_cluster_token_correctly() throws Exception {
        final IdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider(HttpClient.create());

        final Optional<Identity> providerIdentityAsync = keycloakIdentityProvider.getIdentityAsync(getKeycloakToken(), "starter-us-west-1").get();
        softly.assertThat(providerIdentityAsync)
                .isPresent();

        final Optional<Identity> providerIdentity = keycloakIdentityProvider.getIdentity(getKeycloakToken(), "starter-us-west-1");
        softly.assertThat(providerIdentity)
                .isPresent();
    }

    @Test
    public void should_get_empty_when_cluster_is_not_connected() throws Exception {
        final IdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider(HttpClient.create());

        final Optional<Identity> providerIdentityAsync = keycloakIdentityProvider.getIdentityAsync(getKeycloakToken(), "starter-us-west-2").get();
        softly.assertThat(providerIdentityAsync)
                .isEmpty();

        final Optional<Identity> providerIdentity = keycloakIdentityProvider.getIdentity(getKeycloakToken(), "starter-us-west-2");
        softly.assertThat(providerIdentity)
                .isEmpty();
    }

    @Test
    public void should_get_empty_with_invalid_token() throws Exception {
        final IdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider(HttpClient.create());

        final Optional<Identity> gitHubIdentityAsync = keycloakIdentityProvider.getIdentityAsync(TokenIdentity.of("invalid_token"), "github").get();
        softly.assertThat(gitHubIdentityAsync)
                .isEmpty();

        final Optional<Identity> gitHubIdentity = keycloakIdentityProvider.getIdentity(TokenIdentity.of("invalid_token"), "github");
        softly.assertThat(gitHubIdentity)
                .isEmpty();
    }

    private static TokenIdentity getKeycloakToken() {
        return TokenIdentity.of(getRequiredEnvVarOrSysProp("KEYCLOAK_TOKEN"));
    }
}