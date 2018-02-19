package io.fabric8.launcher.osio.steps;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.KubernetesNames;
import io.fabric8.kubernetes.api.ServiceNames;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.osio.tenant.Namespace;
import io.fabric8.launcher.osio.tenant.Tenant;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildRequest;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import io.fabric8.openshift.client.OpenShiftClient;

@ApplicationScoped
public class OpenshiftClient {
    private static Logger log = Logger.getLogger(OpenShiftSteps.class.getName());

    @Inject
    OpenShiftService openShiftService;

    @Inject
    Tenant tenant;

    public void applyBuildConfig(BuildConfig buildConfig, OsioProjectile projectile) {
        Controller controller = new Controller(openShiftService.getOpenShiftClient());
        controller.setNamespace(tenant.getDefaultUserNamespace().getName());
        controller.applyBuildConfig(buildConfig, "from project " + projectile.getOpenShiftProjectName());
    }

    public String getJenkinsUrl() {
        String namespace = tenant.getDefaultUserNamespace().getName();
        return KubernetesHelper.getServiceURL(openShiftService.getOpenShiftClient(), ServiceNames.JENKINS, namespace, "http", true);
    }

    public ConfigMap getConfigMap(String configName) {
        return getResource(configName).get();
    }

    public void createConfigMap(String configName, ConfigMap configMap) {
        getResource(configName).create(configMap);
    }

    public void triggerBuild(String projectName) {
        Namespace namespace = tenant.getDefaultUserNamespace();
        String triggeredBuildName;
        BuildRequest request = new BuildRequestBuilder().
                withNewMetadata().withName(projectName).endMetadata().
                addNewTriggeredBy().withMessage("Forge triggered").endTriggeredBy().
                build();
        try {
            OpenShiftClient openShiftClient = openShiftService.getOpenShiftClient();
            Build build = openShiftClient.buildConfigs().inNamespace(namespace.getName())
                    .withName(projectName).instantiate(request);
            if (build != null) {
                triggeredBuildName = KubernetesHelper.getName(build);
                log.info("Triggered build " + triggeredBuildName);
            } else {
                log.severe("Failed to trigger build for " + namespace + "/" + projectName + " due to: no Build returned");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to trigger build for " + namespace + "/" + projectName + " due to: " + e, e);
        }
    }

    private Resource<ConfigMap, DoneableConfigMap> getResource(String configName) {
        String configMapName = KubernetesNames.convertToKubernetesName(configName, false);
        String namespace = tenant.getDefaultUserNamespace().getName();
        return openShiftService.getOpenShiftClient().configMaps().inNamespace(namespace).withName(configMapName);

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

}
