package io.fabric8.forge.generator.che;

import java.io.File;
import java.net.URL;

import io.fabric8.forge.generator.utils.PomFileXml;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CheStackDetectorTest {

    @Test
    public void shouldDetectAsVertx() throws Exception {
        URL resource = getClass().getResource("vertx-http-pom.xml");
        File pomFile = new File(resource.toURI());
        Document document = CheStackDetector.parseXmlFile(pomFile);
        PomFileXml xml = new PomFileXml(pomFile, document);
        CheStack cheStack = CheStackDetector.detectCheStack(null, xml);
        Assertions.assertThat(cheStack).isEqualTo(CheStack.Vertx);
    }
}
