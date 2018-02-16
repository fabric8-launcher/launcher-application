package io.fabric8.launcher.osio.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.builds.Builds;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.launcher.osio.Annotations;
import io.fabric8.launcher.osio.che.CheStack;
import io.fabric8.launcher.osio.che.CheStackDetector;
import io.fabric8.launcher.osio.jenkins.JenkinsConfigParser;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigSpec;
import io.fabric8.openshift.api.model.BuildStrategy;
import io.fabric8.openshift.api.model.JenkinsPipelineBuildStrategy;
import io.fabric8.launcher.osio.tenant.Namespace;
import io.fabric8.launcher.osio.tenant.Tenant;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildRequest;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class OpenShiftSteps {

    private static Logger log = Logger.getLogger(OpenShiftSteps.class.getName());
    @Inject
    GitService gitService;

    @Inject
    OpenshiftClient openshiftClient;

    public void createBuildConfig(OsioProjectile projectile, GitRepository repository) {
        BuildConfig buildConfig = createBuildConfigObject(projectile, repository);
        String spaceId = getSpaceIdFromSpacePath(projectile.getSpacePath());
        setSpaceNameLabelOnPipeline(spaceId, buildConfig);

        openshiftClient.applyBuildConfig(buildConfig, projectile);
    }

    public void createJenkinsConfigMap(GitRepository repository) {
        String gitOwnerName = gitService.getLoggedUser().getLogin();
        String gitRepoName = repository.getFullName();
        ConfigMap cm = openshiftClient.getConfigMap(gitOwnerName);
        boolean update = true;
        if (cm == null) {
            update = false;
            cm = openshiftClient.createNewConfigMap(gitOwnerName);
        }

        Map<String, String> data = cm.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("root-job", "true");
        data.put("trigger-on-change", "true");

        String configXml = data.get("config.xml");
        JenkinsConfigParser configParser = new JenkinsConfigParser(configXml);
        configParser.setRepository(gitRepoName);
        configParser.setGithubOwner(gitOwnerName);
        data.put("config.xml", configParser.toXml());

        if (update) {
            openshiftClient.updateConfigMap(gitOwnerName, data);
        } else {
            openshiftClient.createConfigMap(gitOwnerName, cm);
        }

    }


    public void triggerBuild(OsioProjectile projectile) {
        Namespace namespace = tenant.getDefaultUserNamespace();
        String triggeredBuildName;
        BuildRequest request = new BuildRequestBuilder().
                withNewMetadata().withName(projectile.getOpenShiftProjectName()).endMetadata().
                addNewTriggeredBy().withMessage("Forge triggered").endTriggeredBy().
                build();
        try {
            OpenShiftClient openShiftClient = openShiftService.getOpenShiftClient();
            Build build = openShiftClient.buildConfigs().inNamespace(namespace.getName())
                    .withName(projectile.getOpenShiftProjectName()).instantiate(request);
            if (build != null) {
                triggeredBuildName = KubernetesHelper.getName(build);
                log.info("Triggered build " + triggeredBuildName);
                return;
            } else {
                log.severe("Failed to trigger build for " + namespace + "/" + projectile.getOpenShiftProjectName() + " due to: no Build returned");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to trigger build for " + namespace + "/" + projectile.getOpenShiftProjectName() + " due to: " + e, e);
        }
    }

    public void createJenkinsJob(OsioProjectile projectile, GitRepository repository) {
    private BuildConfig createBuildConfigObject(OsioProjectile projectile, GitRepository repository) {
        String gitUrl = repository.getGitCloneUri().toString();
        BuildConfig buildConfig = Builds.createDefaultBuildConfig(projectile.getOpenShiftProjectName(), gitUrl, openshiftClient.getJenkinsUrl());
        Map<String, String> currentAnnotations = KubernetesHelper.getOrCreateAnnotations(buildConfig);
        currentAnnotations.putAll(getBuildConfigAnnotations(projectile));
        return buildConfig;
    }

    private Map<String, String> getBuildConfigAnnotations(OsioProjectile projectile) {
        Map<String, String> annotations = new HashMap<>();
        // lets add the annotations so that it looks like its generated by jenkins-sync plugin to minimise duplication
        annotations.put(Annotations.JENKINS_GENERATED_BY, "jenkins");
        annotations.put(Annotations.JENKINS_JOB_PATH, tenant.getUsername() + "/" + projectile.getGitRepositoryName() + "/master");
        CheStack cheStack = CheStackDetector.detectCheStack(projectile.getProjectLocation());
        if (cheStack != null) {
            annotations.put(Annotations.CHE_STACK, cheStack.getId());
        }
        // lets disable jenkins-sync plugin creating the BC as well to avoid possible duplicate
        annotations.put(Annotations.JENKINS_DISABLE_SYNC_CREATE_ON, "jenkins");
        return annotations;
    }

    private String getSpaceIdFromSpacePath(String spacePath) {
        return spacePath.substring(1);
    }

    private void setSpaceNameLabelOnPipeline(String spaceId, BuildConfig buildConfig) {
        KubernetesHelper.getOrCreateLabels(buildConfig).put("space", spaceId);
        BuildConfigSpec spec = buildConfig.getSpec();
        if (spec != null) {
            BuildStrategy strategy = spec.getStrategy();
            if (strategy != null) {
                JenkinsPipelineBuildStrategy jenkinsPipelineStrategy = strategy.getJenkinsPipelineStrategy();
                if (jenkinsPipelineStrategy != null) {
                    setJenkinsSpaceLabel(jenkinsPipelineStrategy, spaceId);
                }
            }
        }
    }

    private void setJenkinsSpaceLabel(JenkinsPipelineBuildStrategy jenkinsPipelineStrategy, String value) {
        List<EnvVar> env = jenkinsPipelineStrategy.getEnv();
        String spaceNameKey = "FABRIC8_SPACE";
        if (env == null) {
            env = new ArrayList<>();
        } else if (env.stream().anyMatch(e -> spaceNameKey.equals(e.getName()))) {
            return;
        }
        env.add(new EnvVarBuilder().withName(spaceNameKey).withValue(value).build());
        jenkinsPipelineStrategy.setEnv(env);
    }
}
