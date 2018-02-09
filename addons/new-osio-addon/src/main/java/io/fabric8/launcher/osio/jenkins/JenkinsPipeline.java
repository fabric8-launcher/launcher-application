package io.fabric8.launcher.osio.jenkins;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableJenkinsPipeline.class)
@JsonDeserialize(as = ImmutableJenkinsPipeline.class)
public interface JenkinsPipeline {
    String getId();

    String getPlatform();

    String getName();

    String getDescription();

    @JsonProperty("suggested")
    boolean isSuggested();

    List<String> getStages();

    @JsonIgnore
    Path getJenkinsfilePath();
}
