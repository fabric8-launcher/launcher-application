package io.fabric8.launcher.core.impl.preparers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.booster.catalog.Booster;
import io.fabric8.launcher.booster.catalog.LauncherConfiguration;
import io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.projectiles.context.CreateProjectileContext;
import org.arquillian.smart.testing.rules.git.server.GitServer;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeArquillianConfigurationPreparerIT {

    @ClassRule
    public static GitServer gitServer = GitServer.bundlesFromDirectory("repos/boosters")
            .usingPort(8765)
            .create();

    private static RhoarBoosterCatalogService boosterCatalogService;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ProvideSystemProperty launcherProperties =
            new ProvideSystemProperty(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REPOSITORY,
                                      "http://localhost:8765/booster-catalog/")
                    .and(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF, "master");

    @BeforeClass
    public static void setUp() throws ExecutionException, InterruptedException {
        boosterCatalogService = buildDefaultCatalogService();
        boosterCatalogService.index().get();
    }

    private static RhoarBoosterCatalogService buildDefaultCatalogService() {
        if (boosterCatalogService == null) {
            RhoarBoosterCatalogService.Builder builder = new RhoarBoosterCatalogService.Builder();
            builder.catalogRepository(LauncherConfiguration.boosterCatalogRepositoryURI());
            builder.catalogRef(LauncherConfiguration.boosterCatalogRepositoryRef());
            builder.transformer((new TestRepoUrlFixer("http://localhost:8765"))::transform);
            boosterCatalogService = builder.build();
        }
        return boosterCatalogService;
    }

    @Test
    public void shouldUpdateArquillianConfiguration() throws IOException {
        // given
        final Optional<RhoarBooster> booster = boosterCatalogService.getBooster(
                BoosterPredicates.withMission(new Mission("rest-http"))
                        .and(BoosterPredicates.withRuntime(new Runtime("vert.x"))
                                     .and(BoosterPredicates.withVersion(new Version("community")))));

        assertThat(booster.isPresent()).isTrue();

        final RhoarBooster rhoarBooster = booster.get();
        final Path rootPath = temporaryFolder.getRoot().toPath();

        boosterCatalogService.copy(rhoarBooster, rootPath);

        final ChangeArquillianConfigurationPreparer arquillianConfigurationPreparer =
                new ChangeArquillianConfigurationPreparer();

        CreateProjectileContext context = mock(CreateProjectileContext.class);
        when(context.getArtifactId()).thenReturn("my-artifact-id");

        // when
        arquillianConfigurationPreparer.prepare(rootPath, rhoarBooster, context);

        // then
        assertThat(
                contentOf(Paths.get(rootPath.toString(), "src/test/resources/arquillian.xml").toFile())
                        .contains("<property name=\"app.name\">my-artifact-id</property>")).isTrue();
    }

    @Test
    public void shouldUpdateArquillianConfigurationForDifferentTestPath() throws IOException {
        // given
        final Optional<RhoarBooster> booster = boosterCatalogService.getBooster(
                BoosterPredicates.withMission(new Mission("circuit-breaker"))
                        .and(BoosterPredicates.withRuntime(new Runtime("vert.x"))
                                     .and(BoosterPredicates.withVersion(new Version("community")))));

        assertThat(booster.isPresent()).isTrue();

        final RhoarBooster rhoarBooster = booster.get();
        final Path rootPath = temporaryFolder.getRoot().toPath();
        boosterCatalogService.copy(rhoarBooster, rootPath);

        final ChangeArquillianConfigurationPreparer arquillianConfigurationPreparer =
                new ChangeArquillianConfigurationPreparer();

        final CreateProjectileContext context = mock(CreateProjectileContext.class);
        when(context.getArtifactId()).thenReturn("my-artifact-id");

        // when
        arquillianConfigurationPreparer.prepare(rootPath, rhoarBooster, context);

        // then
        Assertions.assertThat(
                contentOf(Paths.get(rootPath.toString(), "integration-tests/src/test/resources/arquillian.xml").toFile())
                        .contains("<property name=\"app.name\">my-artifact-id</property>")).isTrue();
    }

    private static class TestRepoUrlFixer {
        private final String fixedUrl;

        TestRepoUrlFixer(String fixedUrl) {
            this.fixedUrl = fixedUrl;
        }

        public Map<String, ? extends Object> transform(Map<String, ? extends Object> data) {
            String gitRepo = Booster.getDataValue(data, "repo", null);
            if (gitRepo != null) {
                Map<String, Object> newdata = new HashMap<>();
                newdata.putAll(data);
                gitRepo = gitRepo.replace("https://github.com", fixedUrl);
                newdata.put("repo", gitRepo);
                data = Collections.unmodifiableMap(newdata);
            }
            return data;
        }
    }
}
