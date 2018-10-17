package io.fabric8.launcher.core.impl.preparers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class ChangeArquillianConfigurationPreparerTest {

   private static final String PROPERTY = "<property name=\"app.name\">foo.bar</property>";

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Before
   public void setUp() throws IOException {
      folder.newFolder("src", "test", "resources");
   }

   @Test
   public void shouldUpdatePropertyAppNameForOpenshiftExtensionInArquillianXml() throws IOException {
      // given
      final Path destination =
         Paths.get(folder.getRoot().getAbsolutePath(), "src", "test", "resources", "arquillian.xml");
      final String resource = getClass().getResource("/configuration/arquillian.xml").getFile();
      Files.copy(Paths.get(resource), destination);

      final ChangeArquillianConfigurationPreparer configurationPreparer =
         new ChangeArquillianConfigurationPreparer();

      // when
      configurationPreparer.updateArquillianConfiguration(Paths.get(folder.getRoot().getAbsolutePath()), "foo.bar");

      // then
      assertThat(contentOf(destination.toFile()).contains(PROPERTY)).isTrue();
   }

   @Test
   public void shouldAddPropertyAppNameIfNotPresentForOpenshiftExtensionInArquillianXml() throws IOException {
      // given
      final Path destination =
         Paths.get(folder.getRoot().getAbsolutePath(), "src", "test", "resources", "arquillian.xml");
      final String resource = getClass().getResource("/configuration/arquillian-without-app-name-property.xml").getFile();
      Files.copy(Paths.get(resource), destination);

      final ChangeArquillianConfigurationPreparer configurationPreparer =
         new ChangeArquillianConfigurationPreparer();

      // when
      configurationPreparer.updateArquillianConfiguration(Paths.get(folder.getRoot().getAbsolutePath()), "foo.bar");

      // then
      assertThat(contentOf(destination.toFile()).contains(PROPERTY)).isTrue();
   }

   @Test
   public void shouldNotUpdatePropertyAppNameForAnyOtherExtensionInArquillianXml() throws IOException {
      // given
      final Path destination =
         Paths.get(folder.getRoot().getAbsolutePath(), "src", "test", "resources", "arquillian.xml");
      final String resource = getClass().getResource("/configuration/arquillian.xml").getFile();
      Files.copy(Paths.get(resource), destination);

      final ChangeArquillianConfigurationPreparer configurationPreparer =
         new ChangeArquillianConfigurationPreparer();

      // when
      configurationPreparer.updateArquillianConfiguration(Paths.get(folder.getRoot().getAbsolutePath()), "foo.bar");

      // then
      assertThat(contentOf(destination.toFile()).contains("<property name=\"app.name\">backend</property>")).isTrue();
   }

   @Test
   public void shouldUpdatePropertyAppNameForOpenshiftExtensionInAllAvailableArquillianXml() throws IOException {
      // given
      folder.newFolder("impl", "src", "test", "resources");

      final Path destination1 =
         Paths.get(folder.getRoot().getAbsolutePath(), "src", "test", "resources", "arquillian.xml");
      final Path destination2 =
         Paths.get(folder.getRoot().getAbsolutePath(), "impl", "src", "test", "resources", "arquillian.xml");
      final String resource = getClass().getResource("/configuration/arquillian.xml").getFile();

      Files.copy(Paths.get(resource), destination1);
      Files.copy(Paths.get(resource), destination2);

      final ChangeArquillianConfigurationPreparer configurationPreparer =
         new ChangeArquillianConfigurationPreparer();

      // when
      configurationPreparer.updateArquillianConfiguration(Paths.get(folder.getRoot().getAbsolutePath()), "foo.bar");

      // then
      assertThat(contentOf(destination1.toFile()).contains(PROPERTY)).isTrue();
      assertThat(contentOf(destination2.toFile()).contains(PROPERTY)).isTrue();
   }

   @Test
   public void shouldNotFailIfProjectDoesNotContainArquillianXml() {
      // given
      final ChangeArquillianConfigurationPreparer configurationPreparer =
         new ChangeArquillianConfigurationPreparer();

      // when
      configurationPreparer.updateArquillianConfiguration(Paths.get(folder.getRoot().getAbsolutePath()), "foo.bar");
   }

   @Test
   public void shouldNotFailIfProjectContainEmptyArquillianXml() throws IOException {
      // given
      folder.newFile("src/test/resources/arquillian.xml");

      final ChangeArquillianConfigurationPreparer configurationPreparer =
         new ChangeArquillianConfigurationPreparer();

      // when
      configurationPreparer.updateArquillianConfiguration(Paths.get(folder.getRoot().getAbsolutePath()), "foo.bar");
   }
}
