package io.fabric8.launcher.osio.jenkins;

import io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;

public class JenkinsGitCredentialsTest {

    private static final HoverflyRule HOVERFLY_RULE = LauncherHoverflyRuleConfigurer.createHoverflyProxy("jenkinscredentials.json", "jenkins.openshift.io");


    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE))
            .around(HOVERFLY_RULE);

    @Test
    public void shouldEnsureCredentials() {
        JenkinsGitCredentials jenkinsGitCredentials = new JenkinsGitCredentials("authorization bearer token", "dummy git token");
        jenkinsGitCredentials.ensureCredentials("edewit");
    }
}