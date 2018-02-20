package io.fabric8.launcher.osio.steps;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioProjectile;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.osio.tenant.ImmutableNamespace;
import io.fabric8.launcher.osio.tenant.ImmutableTenant;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.github.impl.KohsukeGitHubServiceFactoryImpl;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.osio.HoverflyRuleConfigurer.createHoverflyProxy;

public class OpenShiftStepsTest {

    @ClassRule
    public static RuleChain ruleChain = RuleChain
            .outerRule(new ProvideSystemProperty("https.proxyHost", "127.0.0.1")
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

        String openShiftApiURL = EnvironmentVariables.getOpenShiftApiURL();
        Config config = new ConfigBuilder().withMasterUrl(openShiftApiURL).withOauthToken("123")
                .withTrustCerts(true).build();

        OpenshiftClient openshiftClient = new OpenshiftClient();
        steps.openshiftClient = openshiftClient;
        openshiftClient.client = new DefaultKubernetesClient(config);

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
        openshiftClient.tenant = ImmutableTenant.builder().identity(identity)
                .username("edewit").email("me@nerdin.ch").namespaces(elements).build();

        steps.tenant = openshiftClient.tenant;

        //when
        steps.createBuildConfig(projectile, repository);
    }

}