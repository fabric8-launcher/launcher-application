package io.fabric8.launcher.core.impl.preparers;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeNodeJSMetadataPreparerTest {

    private Path projectPath;

    @BeforeEach
    public void setUp(@TempDir Path projectPath) throws Exception {
        this.projectPath = projectPath;
        Files.copy(getClass().getResourceAsStream("/package.json"), projectPath.resolve("package.json"));
    }


    @Test
    public void should_update_version_and_name() throws Exception {
        //given
        ChangeNodeJSMetadataPreparer preparer = new ChangeNodeJSMetadataPreparer();

        LauncherProjectileContext context = mock(LauncherProjectileContext.class);
        when(context.getProjectName()).thenReturn("nodejs-test");
        when(context.getProjectVersion()).thenReturn("1.0.0");

        //when
        preparer.prepare(projectPath, null, context);

        //then
        final File expectedResultFile = new File(getClass().getResource("/package-result.json").getFile());
        final byte[] expected = Files.readAllBytes(expectedResultFile.toPath());
        assertThat(projectPath.resolve("package.json")).exists().hasContent(new String(expected, Charset.defaultCharset()));
    }
}