package io.fabric8.launcher.core.impl.preparers;

import java.nio.file.Files;
import java.nio.file.Path;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.documentation.BoosterReadmeProcessor;
import io.fabric8.launcher.core.api.projectiles.context.CreateProjectileContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ReadmePreparerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path projectPath;

    @Before
    public void setUp() throws Exception {
        projectPath = temporaryFolder.newFolder().toPath();
        Files.write(projectPath.resolve("README.md"), "README".getBytes());
    }


    @Test
    public void should_not_replace_readme_if_properties_do_not_exist() throws Exception {
        Mission mission = new Mission("crud");
        Runtime runtime = new Runtime("spring-boot");

        BoosterReadmeProcessor readmeProcessor = mock(BoosterReadmeProcessor.class);
        when(readmeProcessor.getReadmeTemplate(mission)).thenReturn("Foo");
        when(readmeProcessor.getRuntimeProperties("zip", mission, runtime))
                .thenReturn(emptyMap());
        when(readmeProcessor.processTemplate(anyString(), anyMap())).thenReturn("Changed");
        ReadmePreparer readmePreparer = new ReadmePreparer(readmeProcessor);

        CreateProjectileContext context = mock(CreateProjectileContext.class);
        when(context.getMission()).thenReturn(mission);
        when(context.getRuntime()).thenReturn(runtime);

        readmePreparer.prepare(projectPath, null, context);

        assertThat(projectPath.resolve("README.asciidoc")).doesNotExist();
        assertThat(projectPath.resolve("README.md")).exists().hasContent("README");
    }
}