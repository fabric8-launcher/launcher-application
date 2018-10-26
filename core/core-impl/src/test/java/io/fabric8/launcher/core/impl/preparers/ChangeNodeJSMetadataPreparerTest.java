package io.fabric8.launcher.core.impl.preparers;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNodeJSMetadataPreparerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path projectPath;

    @Before
    public void setUp() throws Exception {
        projectPath = temporaryFolder.newFolder().toPath();
        Files.copy(getClass().getResourceAsStream("/package.json"), projectPath.resolve("package.json"));
    }


    @Test
    public void should_update_version_and_name() throws Exception {
        //given
        ChangeNodeJSMetadataPreparer preparer = new ChangeNodeJSMetadataPreparer();

        //when
        preparer.prepare(projectPath, null, new TestProjectileContext());

        //then
        final File expectedResultFile = new File(getClass().getResource("/package-result.json").getFile());
        final byte[] expected = Files.readAllBytes(expectedResultFile.toPath());
        assertThat(projectPath.resolve("package.json")).exists().hasContent(new String(expected, Charset.defaultCharset()));
    }

    private static class TestProjectileContext implements LauncherProjectileContext {

        @Override
        public String getProjectName() {
            return "nodejs-test";
        }

        @Override
        public String getGitOrganization() {
            return null;
        }

        @Override
        public String getGitRepository() {
            return null;
        }

        @Override
        public String getGroupId() {
            return null;
        }

        @Override
        public String getArtifactId() {
            return null;
        }

        @Override
        public String getProjectVersion() {
            return "1.0.0";
        }

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
    }
}