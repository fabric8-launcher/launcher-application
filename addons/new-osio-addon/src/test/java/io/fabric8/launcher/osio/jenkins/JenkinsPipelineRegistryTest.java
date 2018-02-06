package io.fabric8.launcher.osio.jenkins;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class JenkinsPipelineRegistryTest {

    private static JenkinsPipelineRegistry registry = new JenkinsPipelineRegistry();

    @BeforeClass
    public static void setUp() {
        registry.index();
    }


    @Test
    public void shouldHaveAtLeastOne() {
        assertThat(registry.getPipelines()).isNotEmpty();
    }
}
