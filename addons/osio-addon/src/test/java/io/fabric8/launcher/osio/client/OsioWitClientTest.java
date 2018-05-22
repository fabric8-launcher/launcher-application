package io.fabric8.launcher.osio.client;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;

public class OsioWitClientTest {

    private static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("api.openshift.io|api.prod-preview.openshift.io|auth.openshift.io|auth.prod-preview.openshift.io");

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

    private Space defaultSpace;


    private OsioWitClient getOsioWitClient() {
        return new OsioWitClient(OsioTests.getTestAuthorization(), HttpClient.create());
    }

    @Before
    public void before() {
        defaultSpace = getOsioWitClient().createSpace("test-wit-client");
    }

    @After
    public void after() {
        getOsioWitClient().deleteSpace(defaultSpace.getId());
    }

    @Test
    public void should_get_tenant_correctly() {
        final Tenant tenant = getOsioWitClient().getTenant();
        final Tenant.UserInfo userInfo = tenant.getUserInfo();
        final List<Tenant.Namespace> namespaces = tenant.getNamespaces();
        softly.assertThat(userInfo.getUsername()).isEqualTo("osio-ci-launcher-preview");
        softly.assertThat(userInfo.getEmail()).isEqualTo("osio-ci+launcher+preview@redhat.com");
        softly.assertThat(userInfo.getEmail()).isNotEmpty();
        softly.assertThat(namespaces).hasSize(5);
    }

    @Test
    public void should_find_space_by_id_correctly() {
        final Optional<Space> space = getOsioWitClient().findSpaceById(defaultSpace.getId());
        softly.assertThat(space)
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("name", defaultSpace.getName())
                .hasFieldOrPropertyWithValue("id", defaultSpace.getId());
    }

    @Test
    public void should_create_codebase_correctly() {
        final Optional<Space> space = getOsioWitClient().findSpaceById(defaultSpace.getId());
        getOsioWitClient().createCodeBase(space.get().getId(), "stack", URI.create("https://github.com/ia3andy/hoob.git"));
    }

    @Test
    public void should_create_and_delete_space_correctly() {
        final Space space = getOsioWitClient().createSpace("test-wit-client-create");
        softly.assertThat(space)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "test-wit-client-create")
                .hasFieldOrProperty("id");
        softly.assertThatCode(() -> getOsioWitClient().deleteSpace(space.getId())).doesNotThrowAnyException();
    }

}
