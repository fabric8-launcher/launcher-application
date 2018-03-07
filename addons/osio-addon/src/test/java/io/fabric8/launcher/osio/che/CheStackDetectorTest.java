package io.fabric8.launcher.osio.che;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CheStackDetectorTest {

    @Test
    public void shouldDetectAsVertx() throws Exception {
        URL resource = getClass().getResource("vertx/pom.xml");
        Path projectPath = Paths.get(resource.toURI()).getParent();
        CheStack cheStack = CheStackDetector.detectCheStack(projectPath);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.Vertx);
    }

    @Test
    public void shouldDetectAsWildFlySwarm() throws Exception {
        URL resource = getClass().getResource("wfswarm/pom.xml");
        Path projectPath = Paths.get(resource.toURI()).getParent();
        CheStack cheStack = CheStackDetector.detectCheStack(projectPath);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.WildFlySwarm);
    }

}
