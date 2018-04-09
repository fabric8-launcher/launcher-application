/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.core.impl.documentation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.documentation.BoosterDocumentationStore;
import io.fabric8.launcher.core.api.documentation.BoosterReadmeProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class BoosterReadmeProcessorImplTest {

    private static BoosterDocumentationStore documentationStore;

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        final URI repoUri = BoosterReadmeProcessorImplTest.class.getResource("/repos/documentation").toURI();
        documentationStore = new BoosterDocumentationStoreImpl(ForkJoinPool.commonPool(), () -> Paths.get(repoUri));
    }

    @Test
    public void testReadmeTemplate() throws IOException {
        BoosterReadmeProcessorImpl processor = new BoosterReadmeProcessorImpl(documentationStore);
        String readmeTemplate = processor.getReadmeTemplate(new Mission("crud"));
        assertThat(readmeTemplate).contains("${mission} - ${runtime} Booster");
    }

    @Test
    public void testReadmeWithPropertiesReplacedCD() throws IOException {
        BoosterReadmeProcessor processor = new BoosterReadmeProcessorImpl(documentationStore);
        String template = processor.getReadmeTemplate(new Mission("rest-http"));
        Map<String, String> values = new HashMap<>();
        values.put("mission", "rest-http");
        values.put("runtime", "spring-boot");
        values.putAll(
                processor.getRuntimeProperties("cd", new Mission("rest-http"), new Runtime("spring-boot")));
        String finalDoc = processor.processTemplate(template, values);
        assertThat(finalDoc).doesNotContain("${mission} - ${runtime} Booster")
                .doesNotContain("${localRunCMD}")
                .contains(values.get("localRunCMD"));
    }

    @Test
    public void testReadmeWithPropertiesReplacedZip() throws IOException {
        BoosterReadmeProcessor processor = new BoosterReadmeProcessorImpl(documentationStore);
        String template = processor.getReadmeTemplate(new Mission("rest-http"));
        Map<String, String> values = new HashMap<>();
        values.put("mission", "rest-http");
        values.put("runtime", "spring-boot");
        values.putAll(
                processor.getRuntimeProperties("zip", new Mission("rest-http"),
                                               new Runtime("spring-boot")));
        String finalDoc = processor.processTemplate(template, values);
        assertThat(finalDoc).doesNotContain("${mission} - ${runtime} Booster")
                .doesNotContain("${localRunCMD}")
                .contains(values.get("localRunCMD"));
    }

    @Test
    public void getRuntimePropertiesShouldReturnAnEmptyMapIfNotFound() throws IOException {
        BoosterReadmeProcessor processor = new BoosterReadmeProcessorImpl(documentationStore);
        final Map<String, String> runtimeProperties = processor.getRuntimeProperties("test", new Mission("invalid"), new Runtime("invalid"));
        assertThat(runtimeProperties)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void getReadmeTemplateShouldReturnNullIfNotFound() throws IOException {
        BoosterReadmeProcessor processor = new BoosterReadmeProcessorImpl(documentationStore);
        final String template = processor.getReadmeTemplate(new Mission("invalid"));
        assertThat(template).isNull();
    }
}
