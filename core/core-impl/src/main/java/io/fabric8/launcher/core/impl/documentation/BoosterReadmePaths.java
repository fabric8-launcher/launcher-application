package io.fabric8.launcher.core.impl.documentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.newInputStream;

final class BoosterReadmePaths {

    private BoosterReadmePaths() {
        throw new IllegalAccessError("Utility class");
    }

    private static final String README_TEMPLATE_PATH = "/docs/topics/readme/%s-README.adoc";
    private static final String README_PROPERTIES_PATH = "/docs/topics/readme/%s-%s-%s.properties";


    static Path getReadmeTemplatePath(final String basePath, final String missionId) {
        return Paths.get(basePath, String.format(README_TEMPLATE_PATH, missionId));
    }

    static String getReadmePropertiesFileName(final String deploymentType, final String missionId, final String runtimeId) {
        return String.format(README_PROPERTIES_PATH, deploymentType, missionId, runtimeId);
    }

    static Path getReadmePropertiesPath(final String basePath, final String deploymentType, final String missionId, final String runtimeId) {
        return Paths.get(basePath, getReadmePropertiesFileName(deploymentType, missionId, runtimeId));
    }

    static String loadContents(final Path path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(newInputStream(path)))) {
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
