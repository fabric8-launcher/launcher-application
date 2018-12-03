package io.fabric8.launcher.core.impl.preparers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.IDEGenerationCapable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static io.fabric8.launcher.core.impl.preparers.VSCodePreparer.EXTENSIONS_JSON_FILE;
import static io.fabric8.launcher.core.impl.preparers.VSCodePreparer.VSCODE_FOLDER;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class VSCodePreparerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void checkThatRuntimeAddFiles(String runtimeId) {
        // given
        IDEGenerationCapable context = mock(IDEGenerationCapable.class, withSettings().extraInterfaces(ProjectileContext.class));
        when(context.getRuntime()).thenReturn(new Runtime(runtimeId));
        when(context.getSupportedIDEs()).thenReturn(singletonList("vscode"));
        final VSCodePreparer configurationPreparer = new VSCodePreparer();

        // when
        configurationPreparer.prepare(folder.getRoot().toPath(), null, (ProjectileContext) context);

        // then
        assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER)).exists();
        assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE)).exists();
    }

    private void generateDummyExtensions(Path projectPath, String content) throws IOException {
        Files.createDirectories(projectPath.resolve(VSCODE_FOLDER));
        try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            Files.copy(stream, projectPath.resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE));
        }
    }

    private void checkThatRuntimeDoesNotAddFilesWhenAlready(String runtimeId) throws IOException {
        // given
        IDEGenerationCapable context = mock(IDEGenerationCapable.class, withSettings().extraInterfaces(ProjectileContext.class));
        when(context.getRuntime()).thenReturn(new Runtime(runtimeId));
        when(context.getSupportedIDEs()).thenReturn(singletonList("vscode"));
        generateDummyExtensions(folder.getRoot().toPath(), "dummy " + runtimeId);
        final VSCodePreparer configurationPreparer = new VSCodePreparer();

        // when
        configurationPreparer.prepare(folder.getRoot().toPath(), null, (ProjectileContext) context);

        // then
        assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER)).exists();
        assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE)).exists();
        assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE)).hasContent("dummy " + runtimeId);
    }

    @Test
    public void checkThatSpringBootRuntimeAddFiles() {
        checkThatRuntimeAddFiles("spring-boot");
    }

    @Test
    public void checkThatSpringBootRuntimeDoesNotAddFilesWhenAlready() throws IOException {
        checkThatRuntimeDoesNotAddFilesWhenAlready("spring-boot");
    }

    @Test
    public void checkThatVertxRuntimeAddFiles() {
        checkThatRuntimeAddFiles("vert.x");
    }

    @Test
    public void checkThatVertxRuntimeDoesNotAddFilesWhenAlready() throws IOException {
        checkThatRuntimeDoesNotAddFilesWhenAlready("vert.x");
    }

    @Test
    public void checkThatFuseRuntimeAddFiles() {
        checkThatRuntimeAddFiles("fuse");
    }

    @Test
    public void checkThatFuseRuntimeDoesNotAddFilesWhenAlready() throws IOException {
        checkThatRuntimeDoesNotAddFilesWhenAlready("fuse");
    }

    @Test
    public void checkThatNodeRuntimeAddFiles() {
        checkThatRuntimeAddFiles("nodejs");
    }

    @Test
    public void checkThatNodeRuntimeDoesNotAddFilesWhenAlready() throws IOException {
        checkThatRuntimeDoesNotAddFilesWhenAlready("nodejs");
    }

    @Test
    public void checkThatThorntailRuntimeAddFiles() {
        checkThatRuntimeAddFiles("thorntail");
    }

    @Test
    public void checkThatThorntailRuntimeDoesNotAddFilesWhenAlready() throws IOException {
        checkThatRuntimeDoesNotAddFilesWhenAlready("thorntail");
    }

}
