package io.fabric8.launcher.service.git;

import java.io.File;

import org.arquillian.smart.testing.rules.git.server.GitServer;
import org.eclipse.jgit.api.Git;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class UnmappableCharsTest {

    @ClassRule
    public static GitServer gitServer = GitServer
            .fromBundle("ummappable_chars", "repos/unmappable_chars.bundle")
            .usingPort(8765)
            .create();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void clone_should_succeed() throws Exception {
        File root = folder.newFolder("tmpgit");
        try (Git git = Git.cloneRepository().setURI("http://localhost:8765/ummappable_chars").setDirectory(root).call()) {
            assertThat(new File(root, "aplica\u00E7\u00E3o.yaml")).exists();
        }
    }
}
