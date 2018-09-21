package io.fabric8.launcher.core.impl.preparers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.BoosterCapable;

import static io.fabric8.launcher.core.impl.preparers.VSCodePreparer.EXTENSIONS_JSON_FILE;
import static io.fabric8.launcher.core.impl.preparers.VSCodePreparer.VSCODE_FOLDER;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class VSCodePreparerTest {

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Before
   public void setUp() throws IOException {
   }

   protected void checkThatRuntimeAddFiles(String runtimeId) throws IOException {
      // given
	      BoosterCapable context = mock(BoosterCapable.class, withSettings().extraInterfaces(ProjectileContext.class));
	   Runtime runtime = new Runtime(runtimeId);
	   when(context.getRuntime()).thenReturn(runtime);
      final VSCodePreparer configurationPreparer =
         new VSCodePreparer();
      
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
   
   protected void checkThatRuntimeDoesNotAddFilesWhenAlready(String runtimeId) throws IOException {
      // given
	      BoosterCapable context = mock(BoosterCapable.class, withSettings().extraInterfaces(ProjectileContext.class));
	   Runtime runtime = new Runtime(runtimeId);
	   when(context.getRuntime()).thenReturn(runtime);
	   generateDummyExtensions(folder.getRoot().toPath(), "dummy " + runtimeId);
      final VSCodePreparer configurationPreparer =
         new VSCodePreparer();
      
      // when
      configurationPreparer.prepare(folder.getRoot().toPath(), null, (ProjectileContext) context);

      // then
      assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER)).exists();
      assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE)).exists();
      assertThat(folder.getRoot().toPath().resolve(VSCODE_FOLDER).resolve(EXTENSIONS_JSON_FILE)).hasContent("dummy " + runtimeId);
   }

   @Test
   public void checkThatSpringBootRuntimeAddFiles() throws IOException {
	   checkThatRuntimeAddFiles("spring-boot");
   }
   
   @Test
   public void checkThatSpringBootRuntimeDoesNotAddFilesWhenAlready() throws IOException {
	   checkThatRuntimeDoesNotAddFilesWhenAlready("spring-boot");
   }
   
   @Test
   public void checkThatVertxRuntimeAddFiles() throws IOException {
	   checkThatRuntimeAddFiles("vert.x");
   }
   
   @Test
   public void checkThatVertxRuntimeDoesNotAddFilesWhenAlready() throws IOException {
	   checkThatRuntimeDoesNotAddFilesWhenAlready("vert.x");
   }
   
   @Test
   public void checkThatFuseRuntimeAddFiles() throws IOException {
	   checkThatRuntimeAddFiles("fuse");
   }
   
   @Test
   public void checkThatFuseRuntimeDoesNotAddFilesWhenAlready() throws IOException {
	   checkThatRuntimeDoesNotAddFilesWhenAlready("fuse");
   }
   
   @Test
   public void checkThatNodeRuntimeAddFiles() throws IOException {
	   checkThatRuntimeAddFiles("nodejs");
   }
   
   @Test
   public void checkThatNodeRuntimeDoesNotAddFilesWhenAlready() throws IOException {
	   checkThatRuntimeDoesNotAddFilesWhenAlready("nodejs");
   }
   
   @Test
   public void checkThatThorntailRuntimeAddFiles() throws IOException {
	   checkThatRuntimeAddFiles("thorntail");
   }
   
   @Test
   public void checkThatThorntailRuntimeDoesNotAddFilesWhenAlready() throws IOException {
	   checkThatRuntimeDoesNotAddFilesWhenAlready("thorntail");
   }

}
