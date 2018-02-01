package io.fabric8.launcher.osio.jenkins;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class JenkinsPipelineRegistry {

    public List<JenkinsPipeline> getPipelines() {
        return Arrays.asList(ImmutableJenkinsPipeline.builder()
                                     .id("id")
                                     .name("name")
                                     .description("Description")
                                     .isSuggested(true)
                                     .addStages("Stage 1", "Stage 2")
                                     .build()
        );
    }
}
