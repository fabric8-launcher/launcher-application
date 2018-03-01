package io.fabric8.launcher.osio.steps;

import static io.fabric8.launcher.osio.hoverfly.HoverflyRuleConfigurer.createHoverflyProxy;
import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames.OPENSHIFT_API_URL;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.osio.hoverfly.HoverflySimulationEnvironment;
import io.fabric8.launcher.osio.producers.OpenShiftServiceProducer;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.osio.tenant.ImmutableNamespace;
import io.fabric8.launcher.osio.tenant.ImmutableTenant;
import io.fabric8.launcher.osio.tenant.Tenant;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.github.impl.KohsukeGitHubServiceFactoryImpl;
import io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceImpl;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.RuleChain;

public class OpenShiftStepsTest {

    @ClassRule
    public static RuleChain ruleChain = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(new HoverflySimulationEnvironment()
                .and(OPENSHIFT_API_URL, "https://f8osoproxy-test-dsaas-preview.b6ff.rh-idev.openshiftapps.com"))
            .around(new ProvideSystemProperty("https.proxyHost", "127.0.0.1")
                               .and("https.proxyPort", "8558")
                               .and("javax.net.ssl.trustStore", System.getenv("LAUNCHER_TESTS_TRUSTSTORE_PATH"))
                               .and("javax.net.ssl.trustStorePassword", "changeit"))
            .around(createHoverflyProxy("openshiftsteps-simulation.json",
                                        "github.com|githubusercontent.com|api.openshift.io|api.prod-preview.openshift.io|openshiftapps.com", 8558));

    @Test
    public void shouldCreateBuildConfig() throws IOException, URISyntaxException {
        //given
        OpenShiftSteps steps = new OpenShiftSteps();
        steps.gitService = new KohsukeGitHubServiceFactoryImpl().create(IdentityFactory.createFromUserPassword("edewit", "123"));

        final String expectedName = "my-space";
        File tempDir = Files.createTempDirectory("mc").toFile();

        final OsioProjectile projectile = ImmutableOsioProjectile.builder()
                .mission(new Mission("crud"))
                .runtime(new Runtime("vert.x"))
                .gitRepositoryName("foo")
                .openShiftProjectName(expectedName)
                .projectLocation(tempDir.toPath())
                .spacePath("/my-space")
                .pipelineId("id")
                .build();

        URI uri = new URI("https://github.com/fabric8-launcher/launcher-backend.git");
        GitRepository repository = ImmutableGitRepository.builder().gitCloneUri(uri)
                .fullName("launcher-backend")
                .homepage(new URI("https://dontcare.ch"))
                .build();


        ImmutableNamespace namespace = ImmutableNamespace.builder().name("edewit-osio-jenkins")
                .type("user")
                .clusterUrl("clusterUrl")
                .clusterConsoleUrl("http://conso")
                .build();
        TokenIdentity identity = IdentityFactory.createFromToken("123");
        List<ImmutableNamespace> elements = Collections.singletonList(namespace);
        Tenant tenant = ImmutableTenant.builder().identity(identity)
                .username("edewit").email("me@nerdin.ch").namespaces(elements).build();

        Fabric8OpenShiftServiceImpl openShiftService = new Fabric8OpenShiftServiceFactory(null)
                .create(OpenShiftServiceProducer.OSIO_CLUSTER, IdentityFactory.createFromToken("123"));
        steps.openShiftService = openShiftService;

        steps.tenant = tenant;

        //when
        steps.createBuildConfig(projectile, repository);
    }

}