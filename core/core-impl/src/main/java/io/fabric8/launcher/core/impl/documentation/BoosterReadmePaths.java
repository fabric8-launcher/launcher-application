package io.fabric8.launcher.core.impl.documentation;

import java.nio.file.Path;
import java.nio.file.Paths;

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
}
