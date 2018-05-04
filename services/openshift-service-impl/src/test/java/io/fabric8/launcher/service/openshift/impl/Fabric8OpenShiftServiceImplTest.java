package io.fabric8.launcher.service.openshift.impl;

import java.net.URI;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Fabric8OpenShiftServiceImplTest {

    @Test
    public void should_extract_repository_name_from_regular_URI() {
        URI uri = URI.create("https://github.com/gastaldi/example");
        assertThat(Fabric8OpenShiftServiceImpl.getRepositoryName(uri)).isEqualTo("example");
    }

    @Test
    public void should_extract_repository_name_from_URI_ending_with_slash() {
        URI uri = URI.create("https://github.com/gastaldi/example/");
        assertThat(Fabric8OpenShiftServiceImpl.getRepositoryName(uri)).isEqualTo("example");
    }

    @Test
    public void should_extract_repository_name_from_URI_ending_with_dot_git() {
        URI uri = URI.create("https://github.com/gastaldi/example.git");
        assertThat(Fabric8OpenShiftServiceImpl.getRepositoryName(uri)).isEqualTo("example");
    }

    @Test
    public void should_be_empty_if_no_repository_name_is_found() {
        URI uri = URI.create("https://github.com");
        assertThat(Fabric8OpenShiftServiceImpl.getRepositoryName(uri)).isEmpty();
    }
}