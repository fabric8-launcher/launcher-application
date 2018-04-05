package io.fabric8.launcher.osio.preparers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.fabric8.launcher.osio.jenkins.ImmutableJenkinsPipeline;
import io.fabric8.launcher.osio.jenkins.JenkinsPipeline;
import io.fabric8.launcher.osio.jenkins.JenkinsPipelineRegistry;
import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateJenkinsfilePreparerTest {

    private static final String PIPELINE_ID = "test";

    private CreateJenkinsfilePreparer preparer = new CreateJenkinsfilePreparer();

    private Path basePath;

    private JenkinsPipeline jenkinsPipeline;

    @Before
    public void init() throws URISyntaxException {
        basePath = Paths.get(getClass().getResource("/jenkinsfile-preparer-test").toURI());
        preparer.pipelineRegistry = new JenkinsPipelineRegistry() {
            @Override
            public Optional<JenkinsPipeline> findPipelineById(String pipelineId) {
                return Optional.of(jenkinsPipeline);
            }
        };
    }

    @Test
    public void shouldMergeJenkinsSnippetsIntoJenkinsPipelineFile() throws IOException {
        // given
        Path pipelinePath = getJenkinsFile();
        Path testProject1 = basePath.resolve("test-project1");

        // when
        preparer.prepare(testProject1, null, getOsioProjectileContext());

        // then
        assertJenkinsFileEquals(pipelinePath, "Jenkinsfile-result", testProject1);
    }

    @Test
    public void shouldMergeWhenNoInjectionPoint() throws IOException {
        // given
        Path jenkinsFile = basePath.resolve("pipeline").resolve("no-inject").resolve("Jenkinsfile");
        initPipeline(jenkinsFile);

        Path testProject1 = basePath.resolve("test-project1");

        // when
        preparer.prepare(testProject1, null, getOsioProjectileContext());

        // then
        Path result = testProject1.resolve("Jenkinsfile");
        assertEquals(Files.readAllLines(jenkinsFile), Files.readAllLines(result));
    }

    @Test
    public void shouldMergeWithoutSnippets() throws IOException {
        // given
        Path pipelinePath = getJenkinsFile();
        Path testProject2 = basePath.resolve("test-project2");

        // when
        preparer.prepare(testProject2, null, getOsioProjectileContext());

        // then
        assertJenkinsFileEquals(pipelinePath, "Jenkinsfile-result-empty", testProject2);
    }

    @Test
    public void shouldOverwriteExistingJenkinsFile() throws IOException {
        // given
        Path pipelinePath = getJenkinsFile();
        Path testProject3 = basePath.resolve("test-project3");

        // when
        preparer.prepare(testProject3, null, getOsioProjectileContext());

        // then
        assertJenkinsFileEquals(pipelinePath, "Jenkinsfile-result-empty", testProject3);
    }

    @Test
    public void shouldReplaceMultipleSnippets() throws IOException {
        // given
        Path pipelinePath = basePath.resolve("pipeline").resolve("multi");
        Path jenkinsFile = pipelinePath.resolve("Jenkinsfile");
        initPipeline(jenkinsFile);
        Path testProject4 = basePath.resolve("test-project4");

        // when
        preparer.prepare(testProject4, null, getOsioProjectileContext());

        // then
        assertJenkinsFileEquals(pipelinePath, "Jenkinsfile-result", testProject4);
    }

    private void assertJenkinsFileEquals(Path pipelinePath, String name, Path projectPath) throws IOException {
        Path expectedResultFile = pipelinePath.resolve(name);
        Path result = projectPath.resolve("Jenkinsfile");
        assertEquals(Files.readAllLines(expectedResultFile), Files.readAllLines(result));
    }

    private OsioProjectileContext getOsioProjectileContext() {
        OsioProjectileContext context = mock(OsioProjectileContext.class);
        when(context.getPipelineId()).thenReturn(PIPELINE_ID);
        return context;
    }

    private Path getJenkinsFile() {
        Path pipelinePath = basePath.resolve("pipeline").resolve("inject");
        Path jenkinsFile = pipelinePath.resolve("Jenkinsfile");
        initPipeline(jenkinsFile);
        return pipelinePath;
    }

    private void initPipeline(Path jenkinsFile) {
        this.jenkinsPipeline = ImmutableJenkinsPipeline.builder()
                .platform("osio")
                .name("release")
                .description("")
                .isSuggested(false)
                .isTechPreview(false)
                .jenkinsfilePath(jenkinsFile)
                .id(PIPELINE_ID)
                .build();
    }

}