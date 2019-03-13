package io.fabric8.launcher.service.openshift.impl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SerializationTest {

    @Test
    void should_not_throw_exception() throws Exception {
        InputStream is = Files.newInputStream(Paths.get("src/test/resources/service.cache.yml"));
        assertNotNull(is);
        assertThatCode(() -> Serialization.unmarshal(is, Collections.emptyMap()))
                .doesNotThrowAnyException();
    }
}
