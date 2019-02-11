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
        Assertions.assertThat(cheStack).isEqualTo(CheStack.VERTX);
    }

    @Test
    public void shouldDetectAsWildFlySwarm() throws Exception {
        URL resource = getClass().getResource("wfswarm/pom.xml");
        Path projectPath = Paths.get(resource.toURI()).getParent();
        CheStack cheStack = CheStackDetector.detectCheStack(projectPath);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.WILDFLY_SWARM);
    }

    @Test
    public void shouldDetectAsSpringBoot() throws Exception {
        URL resource = getClass().getResource("spring-boot/pom.xml");
        Path projectPath = Paths.get(resource.toURI()).getParent();
        CheStack cheStack = CheStackDetector.detectCheStack(projectPath);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.SPRING_BOOT);
    }

    @Test
    public void shouldDetectAsNodeJS() throws Exception {
        URL resource = getClass().getResource("nodejs/package.json");
        Path projectPath = Paths.get(resource.toURI()).getParent();
        CheStack cheStack = CheStackDetector.detectCheStack(projectPath);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.NODE_JS);
    }

    @Test
    public void shouldDetectAsGolang() throws Exception {
        URL resource = getClass().getResource("golang/main.go");
        Path projectPath = Paths.get(resource.toURI()).getParent();
        CheStack cheStack = CheStackDetector.detectCheStack(projectPath);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.GOLANG);
    }
}
