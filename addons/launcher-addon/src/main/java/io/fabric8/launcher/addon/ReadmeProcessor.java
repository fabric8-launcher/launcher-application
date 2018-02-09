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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import io.fabric8.launcher.core.api.LauncherProjectileContext;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Reads the contents from the appdev-documentation repository
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Singleton
public class ReadmeProcessor implements ProjectilePreparer {
    private static final String README_TEMPLATE_PATH = "readme/%s-README.adoc";

    private static final String README_PROPERTIES_PATH = "readme/%s-%s-%s.properties";

    private static final Logger logger = Logger.getLogger(ReadmeProcessor.class.getName());

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            return;
        }
        CreateProjectileContext createProjectileContext = (CreateProjectileContext) context;
        try {
            // Create README.adoc file
            String template = getReadmeTemplate(createProjectileContext.getMission());
            if (template != null) {
                Map<String, String> values = new HashMap<>();
                values.put("missionId", createProjectileContext.getMission().getId());
                values.put("mission", createProjectileContext.getMission().getName());
                values.put("runtimeId", createProjectileContext.getRuntime().getId());
                values.put("runtime", createProjectileContext.getRuntime().getName());
                if (createProjectileContext.getRuntimeVersion() != null) {
                    values.put("runtimeVersion", createProjectileContext.getRuntimeVersion().getName());
                } else {
                    values.put("runtimeVersion", "");
                }
                values.put("groupId", createProjectileContext.getGroupId());
                values.put("artifactId", createProjectileContext.getArtifactId());
                values.put("version", createProjectileContext.getProjectVersion());
                String deploymentType = "zip";
                if (context instanceof LauncherProjectileContext) {
                    LauncherProjectileContext createContext = (LauncherProjectileContext) context;
                    values.put("openShiftProject", createContext.getProjectName());
                    values.put("targetRepository", Objects.toString(createContext.getGitRepository(), createContext.getProjectName()));
                    deploymentType = "cd";
                }
                values.putAll(getRuntimeProperties(deploymentType, createProjectileContext.getMission(), createProjectileContext.getRuntime()));
                String readmeOutput = processTemplate(template, values);
                // Write README.adoc
                Files.write(projectPath.resolve("README.adoc"), readmeOutput.getBytes());
                // Delete README.md
                Files.deleteIfExists(projectPath.resolve("README.md"));
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                logger.log(Level.WARNING, "No README.adoc and properties found for " + createProjectileContext.getMission().getId() + " " + createProjectileContext.getRuntime().getId() +
                        ". Check to see if there is a corresponding properties file for your Mission, Runtime, and DeploymentType here: " +
                        "https://github.com/fabric8-launcher/launcher-documentation/tree/master/docs/topics/readme");

            } else {
                logger.log(Level.SEVERE, "Error while creating README.adoc", e);
            }
        }
    }

    public String getReadmeTemplate(Mission mission) throws IOException {
        URL url = getTemplateURL(mission.getId());
        return url == null ? null : loadContents(url);
    }

    @SuppressWarnings("all")
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
