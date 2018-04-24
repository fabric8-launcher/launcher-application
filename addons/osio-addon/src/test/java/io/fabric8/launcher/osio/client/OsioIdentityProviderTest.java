package io.fabric8.launcher.osio.client;

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
    public void should_get_github_token_correctly() throws Exception {
        final IdentityProvider identityProvider = getOsioAuthClient();

        final Optional<Identity> gitHubIdentityAsync = identityProvider.getIdentityAsync(getTestAuthorization(), "github").get();
        softly.assertThat(gitHubIdentityAsync)
                .isPresent()
                .get()
                .isInstanceOf(TokenIdentity.class);

        final Optional<Identity> gitHubIdentity = identityProvider.getIdentity(getTestAuthorization(), "github");
        softly.assertThat(gitHubIdentity)
                .isPresent()
                .get()
                .isInstanceOf(TokenIdentity.class);
    }

    @Test
    public void should_get_cluster_token_correctly() throws Exception {
        final IdentityProvider identityProvider = getOsioAuthClient();

        final Optional<Identity> providerIdentityAsync = identityProvider.getIdentityAsync(getTestAuthorization(), "openshift-v3").get();
        softly.assertThat(providerIdentityAsync)
                .isPresent();

        final Optional<Identity> providerIdentity = identityProvider.getIdentity(getTestAuthorization(), "openshift-v3");
        softly.assertThat(providerIdentity)
                .isPresent();
    }

    @Test
    public void should_get_empty_with_invalid_token() throws Exception {
        final IdentityProvider identityProvider = getOsioAuthClient();

        final Optional<Identity> gitHubIdentityAsync = identityProvider.getIdentityAsync(TokenIdentity.of("invalid_token"), "github").get();
        softly.assertThat(gitHubIdentityAsync)
                .isEmpty();

        final Optional<Identity> gitHubIdentity = identityProvider.getIdentity(TokenIdentity.of("invalid_token"), "github");
        softly.assertThat(gitHubIdentity)
                .isEmpty();
    }

}
