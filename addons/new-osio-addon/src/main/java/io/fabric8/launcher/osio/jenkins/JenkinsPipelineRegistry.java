package io.fabric8.launcher.osio.jenkins;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class JenkinsPipelineRegistry {


    /**
     * Builds the registry index. Should be called only once
     */
    @PostConstruct
    public void index() {
//        try {
//            Path pipelinesPath = Paths.get(getClass().getResource("pipelines").toURI());
//            System.out.println("PIPELINES: " + pipelinesPath);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

    }

    public List<JenkinsPipeline> getPipelines() {
        return Arrays.asList(ImmutableJenkinsPipeline.builder()
                                     .id("id")
                                     .name("name")
                                     .description("Description")
                                     .isSuggested(true)
                                     .jenkinsfilePath(Paths.get(""))
                                     .addStages("Stage 1", "Stage 2")
                                     .build()
        );
    }

    public Optional<JenkinsPipeline> findPipelineById(String pipelineId) {
        return getPipelines().stream().filter(p -> p.getId().equalsIgnoreCase(pipelineId)).findFirst();
    }
}
