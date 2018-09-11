package io.fabric8.launcher.osio.preparers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;
import io.fabric8.launcher.osio.providers.DependencyParamConverter;
import io.fabric8.maven.Maven;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InsertMavenDependenciesPreparerTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void shouldAddDependencies() throws Exception {
        // given
        final Path projectFolder = temporaryFolder.newFolder().toPath();
        Model originalModel = new Model();
        Path pom = projectFolder.resolve("pom.xml");
        Maven.writeModel(originalModel, pom);

        final InsertMavenDependenciesPreparer insertMavenDependenciesPreparer = new InsertMavenDependenciesPreparer();

        // when
        List<Dependency> dependencies = getDependencies();
        OsioProjectileContext projectileContext = mock(OsioProjectileContext.class);
        when(projectileContext.getDependencies()).thenReturn(dependencies);
        insertMavenDependenciesPreparer.prepare(projectFolder, null, projectileContext);

        // then
        final Model model = Maven.readModel(pom);
        assertThat(model.getDependencies())
                .usingFieldByFieldElementComparator()
                .containsAll(dependencies);
    }

    private List<Dependency> getDependencies() {
        List<Dependency> deps = new ArrayList<>();
        String[] dependencies = {"io.vertx:vertx-sql-common:3.5.0", "io.vertx:vertx-jdbc-client:3.5.0"};
        DependencyParamConverter dependencyParamConverter = new DependencyParamConverter();
        for (String dependency : dependencies) {
            deps.add(dependencyParamConverter.fromString(dependency));
        }
        return deps;
    }
}
