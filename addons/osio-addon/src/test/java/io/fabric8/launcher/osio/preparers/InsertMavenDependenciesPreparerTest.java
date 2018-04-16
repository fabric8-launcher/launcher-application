package io.fabric8.launcher.osio.preparers;

import io.fabric8.launcher.base.maven.Maven;
import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.launcher.osio.providers.DependencyParamConverter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class InsertMavenDependenciesPreparerTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.newFolder("booster");
    }

    @Test
    public void shouldAddDependencies() throws IOException, InterruptedException, URISyntaxException {

        // given
        final String boosterPath = getClass().getResource("/booster/pom.xml").getFile();
        Files.copy(Paths.get(boosterPath),
                Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "booster", "pom.xml"));

        final Path rootPath = Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "booster");
        final InsertMavenDependenciesPreparer insertMavenDependenciesPreparer = new InsertMavenDependenciesPreparer();

        // when
        final MyOsioProjectileContext myOsioProjectileContext = new MyOsioProjectileContext();
        insertMavenDependenciesPreparer.prepare(rootPath, null, myOsioProjectileContext);

        // then
        final Path pom = rootPath.resolve("pom.xml");

        final Model model = Maven.readModel(pom);
        for (Dependency dep : myOsioProjectileContext.getDependencies()) {
            assertThat(model.getDependencies().stream().anyMatch(d ->
                    d.getGroupId().equals(dep.getGroupId()) &&
                            d.getArtifactId().equals(dep.getArtifactId()) &&
                            d.getVersion().equals(dep.getVersion())
            )).isTrue();
        }
    }


    private static class MyOsioProjectileContext extends OsioProjectileContext {

        @Override
        public List<Dependency> getDependencies() {
            List<Dependency> deps = new ArrayList<>();
            String[] dependencies = {"io.vertx:vertx-sql-common:3.5.0", "io.vertx:vertx-jdbc-client:3.5.0"};
            DependencyParamConverter dependencyParamConverter = new DependencyParamConverter();
            for (String dependency : dependencies) {
                deps.add(dependencyParamConverter.fromString(dependency));
            }
            return deps;
        }
    }

}
