package io.fabric8.launcher.osio.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.KubernetesNames;
import io.fabric8.kubernetes.api.ServiceNames;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.osio.tenant.Namespace;
import io.fabric8.launcher.osio.tenant.Tenant;
import io.fabric8.openshift.api.model.BuildConfig;

@ApplicationScoped
public class OpenshiftClient {

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    Tenant tenant;

    public void applyBuildConfig(BuildConfig buildConfig, OsioProjectile projectile) {
        Controller controller = new Controller(kubernetesClient);
        controller.setNamespace(getNamespace());
        controller.applyBuildConfig(buildConfig, "from project " + projectile.getOpenShiftProjectName());
    }

    public String getJenkinsUrl() {
        return KubernetesHelper.getServiceURL(kubernetesClient, ServiceNames.JENKINS, getNamespace(), "http", true);
    }

    public ConfigMap getConfigMap(String configName) {
        return getResource(configName).get();
    }

    public void createConfigMap(String configName, ConfigMap configMap) {
        getResource(configName).create(configMap);
    }

    private Resource<ConfigMap, DoneableConfigMap> getResource(String configName) {
        String configMapName = KubernetesNames.convertToKubernetesName(configName, false);
        return kubernetesClient.configMaps().inNamespace(getNamespace()).withName(configMapName);

    }

    public ConfigMap createNewConfigMap(String gitOwnerName) {
        String configMapName = KubernetesNames.convertToKubernetesName(gitOwnerName, false);
        return new ConfigMapBuilder().withNewMetadata().withName(configMapName).
                addToLabels("provider", "fabric8").
                addToLabels("openshift.io/jenkins", "job").endMetadata().withData(new HashMap<>()).build();
    }

    public void updateConfigMap(String configName, Map<String, String> data) {
        getResource(configName).edit().withData(data).done();
    }

    private String getNamespace() {
        String namespace = null;
        List<Namespace> namespaces = tenant.getNamespaces();
        if (!namespaces.isEmpty()) {
            namespace = namespaces.get(0).getName();
        }
        return namespace;
    }

}
