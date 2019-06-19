package io.fabric8.launcher.service.git;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.fabric8.launcher.service.git.api.ImmutableGitServiceConfig;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static io.fabric8.launcher.service.git.spi.GitProviderType.GITHUB;
import static org.junit.Assert.assertEquals;

public class OAuthTokenProviderHoverflyTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("github.com");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);

    private OAuthTokenProvider provider;

    @Before
    public void setup() throws NoSuchPaddingException, NoSuchAlgorithmException {
        provider = new OAuthTokenProviderImpl(HttpClient.create());
    }

    @Test
    public void should_fetch_token() {
        // given
        ImmutableGitServiceConfig config = ImmutableGitServiceConfig.builder()
                .id("GitHub")
                .name("GitHub")
                .apiUrl("https://api.github.com")
                .type(GITHUB)
                .putServerProperties("clientId", "9d858453735afda90545")
                .putServerProperties("clientSecret", "790bc36fad15093768486814a935d3b2ed46115c")
                .putServerProperties("oauthUrl", "https://github.com/login/oauth/access_token")
                .build();

        // when
        String token = provider.getToken("c074e509fb10e78c8387", config);
        String decryptToken = provider.decryptToken(token);

        // then
        assertEquals(decryptToken, "dc1c59cc27f7f5aed5bee7b363a7b2d4779265fa");
    }
}