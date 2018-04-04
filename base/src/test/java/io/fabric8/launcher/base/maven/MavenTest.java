package io.fabric8.launcher.base.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.model.Model;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class MavenTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_model() {
        Path basePom = Paths.get("pom.xml");
        Model model = Maven.readModel(basePom);
        assertThat(model).isNotNull();
        assertThat(model.getPomFile().getAbsolutePath()).isEqualTo(basePom.toAbsolutePath().toString());
        assertThat(model.getParent().getGroupId()).isEqualTo("io.fabric8.launcher");
        assertThat(model.getArtifactId()).isEqualTo("launcher-base");
    }

    @Test
    public void should_write_model() throws IOException {
        File pom = temporaryFolder.newFile("temp-pom.xml");
        Model model = new Model();
        model.setPomFile(pom);
        model.setGroupId("org.example");
        model.setArtifactId("example");
        model.setVersion("1.0");
        Maven.writeModel(model);
        assertThat(Files.readAllLines(pom.toPath()).stream().map(String::trim))
                .contains("<groupId>org.example</groupId>", "<artifactId>example</artifactId>", "<version>1.0</version>");
    }

}
