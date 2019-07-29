package io.fabric8.launcher.operator.cr;

import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class LauncherResourceTest {

    @Test
    void should_deserialize_cr() throws Exception {
        File contents = new File("example/cr_next.yaml");
        LauncherResource resource = Serialization.yamlMapper().readValue(contents, LauncherResource.class);
        assertThat(resource).isNotNull();
        assertThat(resource.getSpec().catalog.repositoryUrl).isEqualTo("http://github.com/fabric8-launcher/launcher-booster-catalog");
        assertThat(resource.getSpec().catalog.repositoryRef).isEqualTo("master");
    }

    @Test
    void should_deserialize_minimum_cr() throws Exception {
        File contents = new File("example/cr_minimum.yaml");
        LauncherResource resource = Serialization.yamlMapper().readValue(contents, LauncherResource.class);
        assertThat(resource).isNotNull();
        assertThat(resource.getSpec().openshift.consoleUrl).isNotNull();
        assertThat(resource.getSpec().git.providers).isNotEmpty();
    }

}