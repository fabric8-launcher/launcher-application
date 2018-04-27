package io.fabric8.launcher.osio.steps;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.builds.Builds;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.osio.Annotations;
import io.fabric8.launcher.osio.che.CheStack;
import io.fabric8.launcher.osio.che.CheStackDetector;
import io.fabric8.launcher.osio.client.Tenant;
import io.fabric8.launcher.osio.jenkins.JenkinsConfigParser;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigSpec;
import io.fabric8.openshift.api.model.BuildStrategy;
import io.fabric8.openshift.api.model.JenkinsPipelineBuildStrategy;

import static io.fabric8.launcher.core.api.events.StatusEventType.OPENSHIFT_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.OPENSHIFT_PIPELINE;
import static io.fabric8.launcher.osio.OsioConfigs.getJenkinsUrl;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class OpenShiftSteps {

    private static final Logger log = Logger.getLogger(OpenShiftSteps.class.getName());

    @Inject
    GitService gitService;

    @Inject
    @Application(Application.ApplicationType.OSIO)
    OpenShiftService openShiftService;

    @Inject
    Tenant tenant;

    /**
     * Creates an Openshift secret in the user namespace
     */
    public void ensureCDGithubSecretExists() {
        String secretName = "cd-github";

        Base64.Encoder encoder = Base64.getEncoder();

        String namespace = tenant.getDefaultUserNamespace().getName();
        String username = encoder.encodeToString(gitService.getLoggedUser().getLogin().getBytes());
        // Always assume that a TokenIdentity is passed here
        String password = encoder.encodeToString(((TokenIdentity) gitService.getIdentity()).getToken().getBytes());
        Secret secret = null;
        Resource<Secret, DoneableSecret> secretResource = openShiftService.getOpenShiftClient().secrets().inNamespace(namespace).withName(secretName);
        try {
            secret = secretResource.get();
        } catch (Exception e) {
            log.log(Level.FINE, "Failed to lookup secret " + namespace + "/" + secretName + " due to: " + e, e);
        }
        if (secret == null ||
                !Objects.equals(username, getSecretData(secret, "username")) ||
                !Objects.equals(password, getSecretData(secret, "password"))) {

            try {
                log.info("Upserting Secret " + namespace + "/" + secretName);
                secretResource.createOrReplace(new SecretBuilder().
                        withNewMetadata().withName(secretName).addToLabels("jenkins", "sync").addToLabels("creator", "fabric8").endMetadata().
                        addToData("username", username).
                        addToData("password", password).
                        build());
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to upsert secret " + namespace + "/" + secretName + " due to: " + e, e);
            }
        }
    }

    public BuildConfig createBuildConfig(OsioProjectile projectile, GitRepository repository) {
        BuildConfig buildConfig = createBuildConfigObject(projectile, repository);
        String spaceName = projectile.getSpace().getName();
        setSpaceNameLabelOnPipeline(spaceName, buildConfig);

        openShiftService.applyBuildConfig(buildConfig, tenant.getDefaultUserNamespace().getName(),
                                          "from project " + projectile.getOpenShiftProjectName());

        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), OPENSHIFT_CREATE));

        return buildConfig;
    }

    public ConfigMap createJenkinsConfigMap(OsioProjectile projectile, GitRepository repository) {
        String namespace = tenant.getDefaultUserNamespace().getName();
        String gitOwnerName = gitService.getLoggedUser().getLogin();
        String gitOrganizationName = projectile.getGitOrganization();
        if (gitOrganizationName != null) {
            gitOwnerName = gitOrganizationName;
        }
        String gitRepoName = projectile.getGitRepositoryName();
        ConfigMap cm = openShiftService.getConfigMap(gitOwnerName, namespace).orElse(null);
        boolean update = true;
        if (cm == null) {
            update = false;
            cm = openShiftService.createNewConfigMap(gitOwnerName);
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
            openShiftService.updateConfigMap(gitOwnerName, namespace, data);
        } else {
            openShiftService.createConfigMap(gitOwnerName, namespace, cm);
        }
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), OPENSHIFT_PIPELINE));
        return cm;
    }

    public void triggerBuild(OsioProjectile projectile) {
        //TODO remove this call (the trigger build should already done by the webhook) and change the countdown latch to 1 in OsioStatusClientEndpoint
        String namespace = tenant.getDefaultUserNamespace().getName();
        openShiftService.triggerBuild(projectile.getOpenShiftProjectName(), namespace);

        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), OPENSHIFT_PIPELINE));
    }

    private BuildConfig createBuildConfigObject(OsioProjectile projectile, GitRepository repository) {
        String gitUrl = repository.getGitCloneUri().toString();
        BuildConfig buildConfig = Builds.createDefaultBuildConfig(projectile.getOpenShiftProjectName(), gitUrl, getJenkinsUrl());
        Map<String, String> currentAnnotations = KubernetesHelper.getOrCreateAnnotations(buildConfig);
        currentAnnotations.putAll(getBuildConfigAnnotations(projectile, repository));
        return buildConfig;
    }

    private Map<String, String> getBuildConfigAnnotations(OsioProjectile projectile, GitRepository gitRepository) {
        Map<String, String> annotations = new HashMap<>();
        // lets add the annotations so that it looks like its generated by jenkins-sync plugin to minimise duplication
        annotations.put(Annotations.JENKINS_GENERATED_BY, "jenkins");
        annotations.put(Annotations.JENKINS_JOB_PATH, gitRepository.getFullName() + "/master");
        CheStack cheStack = CheStackDetector.detectCheStack(projectile.getProjectLocation());
        if (cheStack != null) {
            annotations.put(Annotations.CHE_STACK, cheStack.getId());
        }
        // lets disable jenkins-sync plugin creating the BC as well to avoid possible duplicate
        annotations.put(Annotations.JENKINS_DISABLE_SYNC_CREATE_ON, "jenkins");
        return annotations;
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

    private static String getSecretData(Secret secret, String key) {
        if (secret != null) {
            Map<String, String> data = secret.getData();
            if (data != null) {
                return data.get(key);
            }
        }
        return null;
    }
}
