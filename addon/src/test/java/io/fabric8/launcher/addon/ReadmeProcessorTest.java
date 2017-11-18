/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import io.fabric8.launcher.addon.ReadmeProcessor;
import io.openshift.booster.catalog.DeploymentType;
import io.openshift.booster.catalog.Mission;
import io.openshift.booster.catalog.Runtime;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ReadmeProcessorTest
{

   @BeforeClass
   public static void setUp()
   {
      // System.setProperty(ReadmeProcessor.README_TEMPLATE_URL_PROPERTY,
      // "https://raw.githubusercontent.com/rhoads-zach/documentation/README-refactor/docs/topics/readme/%s-README.adoc");
      // System.setProperty(ReadmeProcessor.README_PROPERTIES_URL_PROPERTY,
      // "https://raw.githubusercontent.com/rhoads-zach/documentation/README-refactor/docs/topics/readme/%s-%s.properties");
   }

   @Test
   public void testReadmeTemplate() throws IOException
   {
      ReadmeProcessor processor = new ReadmeProcessor();
      String readmeTemplate = processor.getReadmeTemplate(new Mission("crud"));
      assertThat(readmeTemplate).contains("${mission} - ${runtime} Booster");
   }

   @Test
   public void testReadmeWithPropertiesReplacedCD() throws IOException
   {
      ReadmeProcessor processor = new ReadmeProcessor();
      String template = processor.getReadmeTemplate(new Mission("rest-http"));
      Map<String, String> values = new HashMap<>();
      values.put("mission", "rest-http");
      values.put("runtime", "spring-boot");
      values.putAll(
               processor.getRuntimeProperties(DeploymentType.CD, new Mission("rest-http"), new Runtime("spring-boot")));
      String finalDoc = processor.processTemplate(template, values);
      assertThat(finalDoc).doesNotContain("${mission} - ${runtime} Booster")
               .doesNotContain("${localRunCMD}")
               .contains(values.get("localRunCMD"));
   }

   @Test
   public void testReadmeWithPropertiesReplacedZip() throws IOException
   {
      ReadmeProcessor processor = new ReadmeProcessor();
      String template = processor.getReadmeTemplate(new Mission("rest-http"));
      Map<String, String> values = new HashMap<>();
      values.put("mission", "rest-http");
      values.put("runtime", "spring-boot");
      values.putAll(
               processor.getRuntimeProperties(DeploymentType.ZIP, new Mission("rest-http"),
                        new Runtime("spring-boot")));
      String finalDoc = processor.processTemplate(template, values);
      assertThat(finalDoc).doesNotContain("${mission} - ${runtime} Booster")
               .doesNotContain("${localRunCMD}")
               .contains(values.get("localRunCMD"));
   }

}
