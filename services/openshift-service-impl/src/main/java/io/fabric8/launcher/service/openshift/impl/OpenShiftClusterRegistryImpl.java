package io.fabric8.launcher.service.openshift.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Singleton;

import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftSettings;
import org.yaml.snakeyaml.Yaml;

import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames.OPENSHIFT_CLUSTERS_CONFIG_FILE;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Singleton
public class OpenShiftClusterRegistryImpl implements OpenShiftClusterRegistry {

    public OpenShiftClusterRegistryImpl() {
        Set<OpenShiftCluster> clusters = new LinkedHashSet<>();
        String apiUrl = OpenShiftSettings.getOpenShiftApiUrl();
        String consoleUrl = OpenShiftSettings.getOpenShiftConsoleUrl();
        if (Objects.toString(apiUrl, "").isEmpty() || Objects.toString(consoleUrl, "").isEmpty()) {
            // If API or the console URL are not specified, use config file
            String configFile = OpenShiftSettings.getOpenShiftClustersConfigFile();
            Objects.requireNonNull(configFile, "Env var " + OPENSHIFT_CLUSTERS_CONFIG_FILE + " must be set");
            Path configFilePath = Paths.get(configFile);
            if (!configFilePath.toFile().isFile()) {
                throw new IllegalArgumentException("Config file " + configFile + " is not a regular file");
            }
            try (BufferedReader reader = Files.newBufferedReader(configFilePath)) {
                Yaml yaml = new Yaml(new OpenShiftClusterConstructor());
                @SuppressWarnings("unchecked")
                List<OpenShiftCluster> configClusters = (List<OpenShiftCluster>) yaml.loadAs(reader, List.class);
                Objects.requireNonNull(configClusters, "Config file " + configFile + " is an invalid YAML file");
                if (configClusters.isEmpty()) {
                    throw new IllegalStateException("No entries found in " + configFile);
                }
                clusters.addAll(configClusters);
                defaultCluster = configClusters.get(0);
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading OpenShift Config file", e);
            }
        } else {
            defaultCluster = new OpenShiftCluster("openshift-v3",
                                                  "Local Minishift",
                                                  "local",
                                                  apiUrl,
                                                  consoleUrl);
            clusters.add(defaultCluster);
        }
        this.clusters = Collections.unmodifiableSet(clusters);
    }

    private final Set<OpenShiftCluster> clusters;

    private final OpenShiftCluster defaultCluster;

    @Override
    public OpenShiftCluster getDefault() {
        return defaultCluster;
    }

    @Override
    public Set<OpenShiftCluster> getClusters() {
        return clusters;
    }
}
