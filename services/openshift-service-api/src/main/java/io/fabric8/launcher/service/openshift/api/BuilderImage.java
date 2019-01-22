package io.fabric8.launcher.service.openshift.api;

import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBuilderImage.class)
public interface BuilderImage {

    String getName();

    @JsonIgnore
    InputStream getTemplateContents();
}
