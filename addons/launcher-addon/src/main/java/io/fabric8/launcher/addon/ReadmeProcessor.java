/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.commons.lang3.text.StrSubstitutor;

import io.fabric8.launcher.addon.ui.booster.DeploymentType;
import io.openshift.booster.catalog.rhoar.Mission;
import io.openshift.booster.catalog.rhoar.Runtime;

/**
 * Reads the contents from the appdev-documentation repository
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Singleton
public class ReadmeProcessor {
    private static final String README_TEMPLATE_PATH = "readme/%s-README.adoc";

    private static final String README_PROPERTIES_PATH = "readme/%s-%s-%s.properties";

    public String getReadmeTemplate(Mission mission) throws IOException {
        URL url = getTemplateURL(mission.getId());
        return url == null ? null : loadContents(url);
    }
    
    @SuppressWarnings("all")
    public Map<String, String> getRuntimeProperties(DeploymentType deploymentType, Mission mission, Runtime runtime) throws IOException {
        Properties props = new Properties();
        
        URL url = getPropertiesURL(deploymentType.name().toLowerCase(), mission.getId(), runtime.getId());
               
        if (url != null) {
            try (InputStream is = url.openStream()) {
                props.load(is);
            }
        } else {
            String propertiesFileName = getPropertiesFileName(deploymentType.name().toLowerCase(), mission.getId(), runtime.getId());
            throw new FileNotFoundException(propertiesFileName);
        }
        
        Map<String, String> map = (Map) props;
        return map;
    }

    public String processTemplate(String template, Map<String, String> values) {
        StrSubstitutor strSubstitutor = new StrSubstitutor(values);
        strSubstitutor.setEnableSubstitutionInVariables(true);
        return strSubstitutor.replace(template);
    }

    URL getTemplateURL(String missionId) {
        return getClass().getClassLoader().getResource(String.format(README_TEMPLATE_PATH, missionId));
    }

    String getPropertiesFileName(String deploymentType, String missionId, String runtimeId) {
        return String.format(README_PROPERTIES_PATH, deploymentType, missionId, runtimeId);
    }
    
    URL getPropertiesURL(String deploymentType, String missionId, String runtimeId) {
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
