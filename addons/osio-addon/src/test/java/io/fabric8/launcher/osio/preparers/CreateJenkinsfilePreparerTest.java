package io.fabric8.launcher.osio.preparers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.fabric8.launcher.osio.jenkins.JenkinsPipelineRegistry;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CreateJenkinsfilePreparerTest {

    private CreateJenkinsfilePreparer preparer;

    @Rule
    public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

    private JenkinsPipelineRegistry registry = new JenkinsPipelineRegistry();

    @Before
    public void setUp() {
        registry.index();
        preparer = new CreateJenkinsfilePreparer(registry);
    }


    @Test
    public void should_replace_jenkinsfile() throws IOException {
        // given
        String pipelineId = "maven-releasestageapproveandpromote";
        Path projectPath = temporaryFolderRule.newFolder().toPath();
        Path oldJenkinsFile = temporaryFolderRule.newFile().toPath();
        Files.write(oldJenkinsFile, "old".getBytes());

        OsioImportProjectileContext context = mock(OsioImportProjectileContext.class);
        when(context.getPipelineId()).thenReturn(pipelineId);

        // execute SUT
        preparer.prepare(projectPath, null, context);

        // assert
        assertThat(projectPath.resolve("Jenkinsfile")).exists()
                .hasSameContentAs(registry.findPipelineById(pipelineId).get().getJenkinsfilePath());
    }
}
