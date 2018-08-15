package io.fabric8.launcher.service.openshift.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames;
import okhttp3.Request;
import org.yaml.snakeyaml.Yaml;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.base.identity.TokenIdentity.of;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Singleton
public class OpenShiftClusterRegistryImpl implements OpenShiftClusterRegistry {

    private final HttpClient httpClient;

    private static final String CLUSTER_SUBSCRIPTION_PATTERN = "https://manage.openshift.com/api/accounts/%s/subscriptions?authorization_username=rhdp-launch";

    @Inject
    public OpenShiftClusterRegistryImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
        Set<OpenShiftCluster> clusters = new LinkedHashSet<>();
        String apiUrl = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL.value();
        String consoleUrl = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL.value();
        if (Objects.toString(apiUrl, "").isEmpty() || Objects.toString(consoleUrl, "").isEmpty()) {
            // If API or the console URL are not specified, use config file
            String configFile = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE.value();
            Objects.requireNonNull(configFile, "Env var " + OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE + " must be set");
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
                                                  "Local OpenShift Cluster",
                                                  "local",
                                                  apiUrl,
                                                  consoleUrl);
            clusters.add(defaultCluster);
        }
        this.clusters = Collections.unmodifiableSet(clusters);
    }

    public OpenShiftClusterRegistryImpl() {
        this(HttpClient.create());
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

    @Override
    public Set<OpenShiftCluster> getSubscribedClusters(Principal principal) {
        String token = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_SUBSCRIPTION_TOKEN.value();
        if (token == null || principal == null) {
            // Token does not exist or user is not authenticated, just return all clusters
            return getClusters();
        }
        String url = String.format(CLUSTER_SUBSCRIPTION_PATTERN, principal.getName());
        Request request = securedRequest(of(token.trim())).url(url).build();
        return httpClient.executeAndParseJson(request, tree -> {
            Set<OpenShiftCluster> clusterSet = new HashSet<>();
            for (JsonNode subscription : tree.get("subscriptions")) {
                String clusterId = subscription.get("plan").get("name").asText();
                findClusterById(clusterId).ifPresent(clusterSet::add);
            }
            return clusterSet;
        }).orElseGet(this::getClusters);
    }
}