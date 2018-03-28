package io.fabric8.launcher.osio.jenkins;

import java.util.Optional;

import io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.Tenant;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JenkinsGitCredentialsTest {

    private static final HoverflyRule HOVERFLY_RULE = LauncherHoverflyRuleConfigurer.createHoverflyProxy("jenkinscredentials.json", "jenkins.openshift.io|jenkins.prod-preview.openshift.io");


    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE))
            .around(HOVERFLY_RULE);

    @Test
    public void shouldEnsureCredentials() {
        final IdentityProvider identityProvider = mock(IdentityProvider.class);
        when(identityProvider.getIdentity(Mockito.matches(IdentityProvider.ServiceType.GITHUB)))
                .thenReturn(Optional.of(createFromToken("dummy git token")));
        final Tenant tenant = mock(Tenant.class);
        when(tenant.getIdentity())
                .thenReturn(createFromToken("authorization bearer token"));
        JenkinsGitCredentials jenkinsGitCredentials = new JenkinsGitCredentials(tenant, identityProvider);
        jenkinsGitCredentials.ensureCredentials("edewit");
    }
}