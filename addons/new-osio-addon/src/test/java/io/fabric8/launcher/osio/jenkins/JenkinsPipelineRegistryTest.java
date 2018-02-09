package io.fabric8.launcher.osio.jenkins;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(registry.getPipelines(null)).isNotEmpty();
    }

    @Test
    public void idsAreSorted() {
        Collection<JenkinsPipeline> pipelines = registry.getPipelines("maven");
        assertThat(pipelines).extracting("id").isSorted();
    }
}
