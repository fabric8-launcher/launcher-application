package io.fabric8.launcher.core.impl.identity;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.base.EnvironmentSupport;
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

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.core.impl.identity.KeycloakIdentityProvider.LAUNCHER_MISSIONCONTROL_KEYCLOAK_REALM;
import static io.fabric8.launcher.core.impl.identity.KeycloakIdentityProvider.LAUNCHER_MISSIONCONTROL_KEYCLOAK_URL;

public class KeycloakIdentityProviderHoverflyTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("sso.openshift.io");
    
    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE)
                               .andForSimulationOnly("KEYCLOAK_TOKEN", "efbheifb279272FUHUEHFHUEHF")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_KEYCLOAK_URL, "https://sso.openshift.io/auth")
                               .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_KEYCLOAK_REALM, "rh-developers-launch"))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void shouldGetGitHubTokenCorrectly() throws ExecutionException, InterruptedException {
        final IdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider(HttpClient.createForTest());
        final Optional<Identity> gitHubIdentity = keycloakIdentityProvider.getIdentityAsync(getKeycloakToken(), "github").get();
        softly.assertThat(gitHubIdentity)
                .isPresent()
                .get()
                .isInstanceOf(TokenIdentity.class);
    }

    @Test
    public void shouldGetProviderTokenCorrectly() throws ExecutionException, InterruptedException {
        final IdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider(HttpClient.createForTest());
        final Optional<Identity> providerIdentity = keycloakIdentityProvider.getIdentityAsync(getKeycloakToken(), "starter-us-west-1").get();
        softly.assertThat(providerIdentity)
                .isPresent();
    }

    private static TokenIdentity getKeycloakToken() {
        return TokenIdentity.of(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp("KEYCLOAK_TOKEN"));
    }
}