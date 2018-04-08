/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.core.impl.readme;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.readme.ReadmeProcessor;
import org.apache.commons.lang3.text.StrSubstitutor;

@ApplicationScoped
public class ReadmeProcessorImpl implements ReadmeProcessor {
    private static final String README_TEMPLATE_PATH = "readme/%s-README.adoc";

    private static final String README_PROPERTIES_PATH = "readme/%s-%s-%s.properties";

    @Override
    public String getReadmeTemplate(Mission mission) throws IOException {
        URL url = getTemplateURL(mission.getId());
        return url == null ? null : loadContents(url);
    }

    @Override
    public Map<String, String> getRuntimeProperties(String deploymentType, Mission mission, Runtime runtime) throws IOException {
        Properties props = new Properties();

        URL url = getPropertiesURL(deploymentType, mission.getId(), runtime.getId());

        if (url != null) {
            try (InputStream is = url.openStream()) {
                props.load(is);
            }
        } else {
            String propertiesFileName = getPropertiesFileName(deploymentType, mission.getId(), runtime.getId());
            throw new FileNotFoundException(propertiesFileName);
        }

        @SuppressWarnings("unchecked") Map<String, String> map = (Map) props;
        return map;
    }

    @Override
    public String processTemplate(String template, Map<String, String> values) {
        StrSubstitutor strSubstitutor = new StrSubstitutor(values);
        strSubstitutor.setEnableSubstitutionInVariables(true);
        return strSubstitutor.replace(template);
    }

    URL getTemplateURL(String missionId) {
        return getClass().getClassLoader().getResource(String.format(README_TEMPLATE_PATH, missionId));
    }

    private String getPropertiesFileName(String deploymentType, String missionId, String runtimeId) {
        return String.format(README_PROPERTIES_PATH, deploymentType, missionId, runtimeId);
    }

    private URL getPropertiesURL(String deploymentType, String missionId, String runtimeId) {
        return getClass().getClassLoader().getResource(
                String.format(README_PROPERTIES_PATH, deploymentType, missionId, runtimeId));
    }

    private String loadContents(URL url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int c;
            while ((c = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, c);
            }
            return writer.toString();
        }
    }

}
