package io.fabric8.launcher.core.impl.preparers;

import io.fabric8.launcher.booster.catalog.Booster;
import io.fabric8.launcher.booster.catalog.BoosterDataTransformer;
import io.fabric8.launcher.booster.catalog.LauncherConfiguration;
import io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
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
         builder.transformer(new TestRepoUrlFixer("http://localhost:8765"));
         boosterCatalogService = builder.build();
      }
      return boosterCatalogService;
   }

   @Test
   public void shouldUpdateArquillianConfiguration() throws IOException, ExecutionException, InterruptedException {
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

      // when
      arquillianConfigurationPreparer.prepare(rootPath, rhoarBooster, new MyArquillianProjectileContext());

      // then
      assertThat(
         contentOf(Paths.get(rootPath.toString(), "src/test/resources/arquillian.xml").toFile())
            .contains("<property name=\"app.name\">my-artifact-id</property>"));
   }

   @Test
   public void shouldNotUpdateArquillianConfiguration() throws IOException, ExecutionException, InterruptedException {
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

      // when
      arquillianConfigurationPreparer.prepare(rootPath, rhoarBooster,
         new MyArquillianProjectileContext());

      // then
      Assertions.assertThat(
         contentOf(Paths.get(rootPath.toString(), "integration-tests/src/test/resources/arquillian.xml").toFile())
            .contains("<property name=\"app.name\">my-artifact-id</property>")).isTrue();
   }

   private static class MyArquillianProjectileContext implements CreateProjectileContext {

      @Override
      public Mission getMission() {
         return null;
      }

      @Override
      public Runtime getRuntime() {
         return null;
      }

      @Override
      public Version getRuntimeVersion() {
         return null;
      }

      @Override
      public String getGroupId() {
         return null;
      }

      @Override
      public String getArtifactId() {
         return "my-artifact-id";
      }

      @Override
      public String getProjectVersion() {
         return null;
      }
   }

   private static class TestRepoUrlFixer implements BoosterDataTransformer {
      private final String fixedUrl;

      TestRepoUrlFixer(String fixedUrl) {
         this.fixedUrl = fixedUrl;
      }

      @Override
      public Map<String, Object> transform(Map<String, Object> data) {
         String gitRepo = Booster.getDataValue(data, "source/git/url", null);
         if (gitRepo != null) {
            gitRepo = gitRepo.replace("https://github.com", fixedUrl);
            Booster.setDataValue(data, "source/git/url", gitRepo);
         }
         return data;
      }
   }
}
