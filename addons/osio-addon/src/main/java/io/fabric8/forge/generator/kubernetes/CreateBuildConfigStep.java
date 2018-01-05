/**
 * Copyright 2005-2015 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.forge.generator.kubernetes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import io.fabric8.forge.generator.Annotations;
import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.forge.generator.cache.CacheFacade;
import io.fabric8.forge.generator.cache.CacheNames;
import io.fabric8.forge.generator.che.CheStack;
import io.fabric8.forge.generator.che.CheStackDetector;
import io.fabric8.forge.generator.git.GitAccount;
import io.fabric8.forge.generator.git.GitClonedRepoDetails;
import io.fabric8.forge.generator.git.GitProvider;
import io.fabric8.forge.generator.git.WebHookDetails;
import io.fabric8.forge.generator.pipeline.AbstractDevToolsCommand;
import io.fabric8.forge.generator.tenant.NamespaceDTO;
import io.fabric8.forge.generator.tenant.Tenants;
import io.fabric8.forge.generator.utils.DomUtils;
import io.fabric8.forge.generator.utils.MavenHelpers;
import io.fabric8.forge.generator.utils.PomFileXml;
import io.fabric8.forge.generator.utils.WebClientHelpers;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.KubernetesNames;
import io.fabric8.kubernetes.api.ServiceNames;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigSpec;
import io.fabric8.openshift.api.model.BuildRequest;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import io.fabric8.openshift.api.model.BuildStrategy;
import io.fabric8.openshift.api.model.JenkinsPipelineBuildStrategy;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.utils.DomHelper;
import io.fabric8.utils.IOHelpers;
import io.fabric8.utils.Strings;
import io.fabric8.utils.URLUtils;
import io.fabric8.utils.XmlUtils;
import org.infinispan.Cache;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static io.fabric8.forge.generator.keycloak.TokenHelper.getMandatoryAuthHeader;
import static io.fabric8.forge.generator.kubernetes.Base64Helper.base64decode;
import static io.fabric8.project.support.BuildConfigHelper.createBuildConfig;

/**
 * Creates the BuildConfig in OpenShift/Kubernetes so that the Jenkins build will be created
 */
public class CreateBuildConfigStep extends AbstractDevToolsCommand implements UICommand {
    protected static final String GITHUB_SCM_NAVIGATOR_ELEMENT = "org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator";

    protected static final String REGEX_SCM_SOURCE_FILTER_TRAIT_ELEMENT = "jenkins.scm.impl.trait.RegexSCMSourceFilterTrait";

    private static final transient Logger LOG = LoggerFactory.getLogger(CreateBuildConfigStep.class);

    protected Cache<String, List<NamespaceDTO>> namespacesCache;

    @Inject
    @WithAttributes(label = "Jenkins Space", required = true, description = "The space running Jenkins")
    private UISelectOne<String> jenkinsSpace;

    @Inject
    @WithAttributes(label = "Trigger build", description = "Should a build be triggered immediately after import?")
    private UIInput<Boolean> triggerBuild;

    @Inject
    @WithAttributes(label = "Add CI?", description = "Should we add a Continuous Integration webhooks for Pull Requests?")
    private UIInput<Boolean> addCIWebHooks;

    @Inject
    private CacheFacade cacheManager;

    private KubernetesClientHelper kubernetesClientHelper;

    private int retryTriggerBuildCount = 5;

    private boolean useUiidForBotSecret = true;

    private List<NamespaceDTO> namespaces;

    @Inject
    private KubernetesClientFactory kubernetesClientFactory;

    /**
     * Combines the job patterns.
     */
    public static String combineJobPattern(String oldPattern, String repoName) {
        if (oldPattern == null) {
            oldPattern = "";
        }
        oldPattern = oldPattern.trim();
        if (oldPattern.isEmpty()) {
            return repoName;
        }
        return oldPattern + "|" + repoName;
    }

    public static void closeQuietly(Client client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                LOG.debug("Ignoring exception closing client: " + e, e);
            }
        }
    }

    public void initializeUI(final UIBuilder builder) {
        this.kubernetesClientHelper = kubernetesClientFactory.createKubernetesClient(builder.getUIContext());
        this.namespacesCache = cacheManager.getCache(CacheNames.USER_NAMESPACES);
        final String key = kubernetesClientHelper.getUserCacheKey();
        this.namespaces = namespacesCache.computeIfAbsent(key, k -> Tenants.loadNamespaces(getMandatoryAuthHeader(builder.getUIContext())));

        jenkinsSpace.setValueChoices(Tenants.jenkinsNamespaces(namespaces));
        if (!namespaces.isEmpty()) {
            jenkinsSpace.setDefaultValue(Tenants.findDefaultJenkinsNamespace(namespaces));
        }

        triggerBuild.setDefaultValue(true);
        addCIWebHooks.setDefaultValue(true);

        if (namespaces.size() > 1) {
            builder.add(jenkinsSpace);
        }
        builder.add(triggerBuild);
        builder.add(addCIWebHooks);
    }

    @Override
    public Result execute(UIExecutionContext context) {
        UIContext uiContext = context.getUIContext();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();

        String namespace = (String) attributeMap.get(AttributeMapKeys.NAMESPACE);
        if (Strings.isNullOrBlank(namespace)) {
            // we probably didn't come from the ChoosePipelineStep?
            namespace = Tenants.findDefaultUserNamespace(namespaces);
        }
        String jenkinsNamespace = jenkinsSpace.getValue();
        if (Strings.isNullOrBlank(jenkinsNamespace)) {
            jenkinsNamespace = namespace;
        }
        if (Strings.isNullOrBlank(namespace)) {
            return Results.fail("No attribute: " + AttributeMapKeys.NAMESPACE);
        }
        String projectName = getProjectName(uiContext);
        GitAccount details = (GitAccount) attributeMap.get(AttributeMapKeys.GIT_ACCOUNT);
        if (details == null) {
            return Results.fail("No attribute: " + AttributeMapKeys.GIT_ACCOUNT);

        }
        String gitRepoPattern = (String) attributeMap.get(AttributeMapKeys.GIT_REPOSITORY_PATTERN);
        String gitRepoNameValue = (String) attributeMap.get(AttributeMapKeys.GIT_REPO_NAME);
        Iterable<String> gitRepoNames = (Iterable<String>) attributeMap.get(AttributeMapKeys.GIT_REPO_NAMES);
        if (Strings.isNullOrBlank(gitRepoNameValue) && gitRepoNames == null) {
            return Results.fail("No attribute: " + AttributeMapKeys.GIT_REPO_NAME + " or " + AttributeMapKeys.GIT_REPO_NAMES);
        }
        List<String> gitRepoNameList = new ArrayList<>();
        if (Strings.isNotBlank(gitRepoNameValue)) {
            gitRepoNameList.add(gitRepoNameValue);
        } else {
            for (String gitRepoName : gitRepoNames) {
                gitRepoNameList.add(gitRepoName);
            }
        }
        if (Strings.isNullOrBlank(gitRepoNameValue) && !gitRepoNameList.isEmpty()) {
            gitRepoNameValue = gitRepoNameList.get(0);
        }
        if (Strings.isNullOrBlank(gitRepoNameValue)) {
            gitRepoNameValue = projectName;
        }
        String gitOwnerName = (String) attributeMap.get(AttributeMapKeys.GIT_OWNER_NAME);
        if (Strings.isNullOrBlank(gitOwnerName)) {
            gitOwnerName = details.getUsername();
        }
        GitProvider gitProvider = (GitProvider) attributeMap.get(AttributeMapKeys.GIT_PROVIDER);
        if (gitProvider == null) {
            return Results.fail("No attribute: " + AttributeMapKeys.GIT_PROVIDER);
        }
        KubernetesClientHelper kubernetesClientHelper = getKubernetesClientHelper();
        Controller controller = new Controller(kubernetesClientHelper.getKubernetesClient());
        controller.setNamespace(namespace);
        OpenShiftClient openShiftClient = controller.getOpenShiftClientOrNull();
        if (openShiftClient == null) {
            return Results.fail("Could not create OpenShiftClient. Maybe the Kubernetes server version is older than 1.7?");
        }

        String jenkinsJobUrl = null;
        String cheStackId = null;
        String message;
        Boolean addCI = addCIWebHooks.getValue();
        boolean isGitHubOrganisationFolder = gitProvider.isGitHub();
        String gitToken = details.tokenOrPassword();

        boolean talkToJenkins = false;
        String useJenkinsFlag = System.getenv("USE_JENKINS");
        if (useJenkinsFlag != null && useJenkinsFlag.equalsIgnoreCase("true")) {
            talkToJenkins = true;
        }

        List<GitRepoDTO> gitRepos = getGitRepos(uiContext, gitRepoNameValue);
        StringBuilder messageBuilder = new StringBuilder();
        for (GitRepoDTO gitRepo : gitRepos) {
            String gitUrl = gitRepo.getUrl();
            gitRepoNameValue = gitRepo.getRepoName();
            projectName = KubernetesNames.convertToKubernetesName(gitRepo.getRepoName(), false);
            if (Strings.isNullOrBlank(gitUrl) || Strings.isNullOrBlank(projectName)) {
                LOG.warn("Invalid GitRepo " + gitRepo);
                continue;
            }

            if (addCI && isGitHubOrganisationFolder) {
                ensureCDGihubSecretExists(kubernetesClientHelper.getKubernetesClient(), namespace, gitOwnerName, gitToken);
            }
            try {
                BuildConfig oldBC = openShiftClient.buildConfigs().inNamespace(namespace).withName(projectName).get();
                if (oldBC != null && Strings.isNotBlank(KubernetesHelper.getName(oldBC))) {
                    LOG.warn("Already created build " + namespace + "/" + projectName + " so returning");
                    return Results.fail("Already created BuildConfig " + namespace + "/" + projectName);
                }
            } catch (Exception e) {
                LOG.warn("Ignoring exception looking up BuildConfig " + namespace + "/" + projectName + ": " + e, e);
            }

            Map<String, String> annotations = new HashMap<>();
            // lets add the annotations so that it looks like its generated by jenkins-sync plugin to minimise duplication
            if (addCI && isGitHubOrganisationFolder) {
                annotations.put(Annotations.JENKINGS_GENERATED_BY, "jenkins");
                annotations.put(Annotations.JENKINS_JOB_PATH, "" + gitOwnerName + "/" + gitRepoNameValue + "/master");
            }
            String project = getProjectName(uiContext);
            File pom = null;
            if (project == null) { // if no project (only quickstart flow), we are in "import repo" flow
                Object obj = attributeMap.get(AttributeMapKeys.GIT_CLONED_REPOS); // let's find the cloned repo directory
                if ((obj != null) && (obj instanceof ArrayList)) {
                    ArrayList<GitClonedRepoDetails> list = (ArrayList<GitClonedRepoDetails>) obj;
                    for (GitClonedRepoDetails repoDetails : list) {
                        if (repoDetails.getGitRepoName().equals(gitRepo.getRepoName())) {
                            File dir = repoDetails.getAttributes().getDirectory();
                            pom = new File(dir, "pom.xml");
                        }
                    }
                }
            }

            PomFileXml pomFile = MavenHelpers.findPom(uiContext, pom);
            CheStack stack = CheStackDetector.detectCheStack(uiContext, pomFile);
            if (stack != null) {
                cheStackId = stack.getId();
                annotations.put(Annotations.CHE_STACK, cheStackId);
            }
            if (addCI && isGitHubOrganisationFolder) {
                // lets disable jenkins-syn plugin creating the BC as well to avoid possible duplicate
                annotations.put("jenkins.openshift.org/disable-sync-create-on", "jenkins");
            }

            BuildConfig buildConfig = createBuildConfig(kubernetesClientHelper.getKubernetesClient(), namespace, projectName, gitUrl, annotations);
            String spaceId = null;
            Object spaceValue = attributeMap.get(AttributeMapKeys.SPACE);
            if (spaceValue instanceof SpaceDTO) {
                SpaceDTO spaceDTO = (SpaceDTO) spaceValue;
                spaceId = spaceDTO.getId();
            } else if (spaceValue instanceof String) {
                spaceId = (String) spaceValue;
            }
            LOG.info("Got labelSpace: " + spaceId + " for new app " + projectName + " for user " + gitOwnerName);

            if (Strings.isNotBlank(spaceId)) {
                KubernetesHelper.getOrCreateLabels(buildConfig).put("space", spaceId);
                BuildConfigSpec spec = buildConfig.getSpec();
                if (spec != null) {
                    BuildStrategy strategy = spec.getStrategy();
                    if (strategy != null) {
                        JenkinsPipelineBuildStrategy jenkinsPipelineStrategy = strategy.getJenkinsPipelineStrategy();
                        if (jenkinsPipelineStrategy != null) {
                            ensureEnvVar(jenkinsPipelineStrategy, "FABRIC8_SPACE", spaceId);
                        }
                    }
                }
            }
            controller.applyBuildConfig(buildConfig, "from project " + projectName);

            messageBuilder.append("Created OpenShift BuildConfig ").append(namespace).append("/").append(projectName);
        }

        message = messageBuilder.toString();
        List<String> warnings = new ArrayList<>();

        if (addCI) {
            String discoveryNamespace = kubernetesClientHelper.getDiscoveryNamespace(jenkinsNamespace);
            String jenkinsUrl = null;
            try {
                jenkinsUrl = KubernetesHelper.getServiceURL(kubernetesClientHelper.getKubernetesClient(), ServiceNames.JENKINS, jenkinsNamespace, "https", true);
                discoveryNamespace = jenkinsNamespace;
            } catch (Exception e) {
                if (!discoveryNamespace.equals(jenkinsNamespace)) {
                    try {
                        jenkinsUrl = KubernetesHelper.getServiceURL(kubernetesClientHelper.getKubernetesClient(), ServiceNames.JENKINS, discoveryNamespace, "https", true);
                    } catch (Exception e2) {
                        throw new BadTenantException("Failed to find Jenkins URL in namespaces " + discoveryNamespace + " and " + jenkinsNamespace + ": " + e, e);
                    }
                }
            }
            if (Strings.isNullOrBlank(jenkinsUrl)) {
                throw new BadTenantException("Failed to find Jenkins URL in namespace " + discoveryNamespace);
            }

            String botServiceAccount = "cd-bot";
            String botSecret = findBotSecret(discoveryNamespace, botServiceAccount);
            if (Strings.isNullOrBlank(botSecret)) {
                botSecret = "secret101";
            }
            String oauthToken = kubernetesClientHelper.getKubernetesClient().getConfiguration().getOauthToken();
            String authHeader = "Bearer " + oauthToken;

            String webhookUrl = URLUtils.pathJoin(jenkinsUrl, "/github-webhook/");

            if (isGitHubOrganisationFolder) {
                if (talkToJenkins) {
                    try {
                        ensureJenkinsCDCredentialCreated(gitOwnerName, gitToken, jenkinsUrl, authHeader);
                    } catch (Exception e) {
                        LOG.error("Failed to create Jenkins CD Bot credentials: " + e, e);
                        return Results.fail("Failed to create Jenkins CD Bot credentials: " + e, e);
                    }
                }

                String gitRepoPatternOrName = gitRepoPattern;
                if (Strings.isNullOrBlank(gitRepoPatternOrName)) {
                    gitRepoPatternOrName = Strings.join(gitRepoNameList, "|");
                }
                String jobUrl = URLUtils.pathJoin(jenkinsUrl, "/job/" + gitOwnerName);
                if (Strings.isNotBlank(message)) {
                    message += ". ";
                }
                message += "Created Jenkins job: " + jobUrl;
                if (talkToJenkins) {
                    try {
                        jenkinsJobUrl = jobUrl;
                        ensureJenkinsCDOrganisationJobCreated(jenkinsUrl, jobUrl, oauthToken, authHeader, gitOwnerName, gitRepoPatternOrName);
                    } catch (Exception e) {
                        LOG.error("Failed to create Jenkins Organisation job: " + e, e);
                        return Results.fail("Failed to create Jenkins Organisation job:: " + e, e);
                    }
                } else {
                    try {
                        ensureJenkinsCDOrganisationConfigMapCreated(kubernetesClientHelper.getKubernetesClient(), namespace, gitOwnerName, gitRepoPatternOrName);
                    } catch (Exception e) {
                        LOG.error("Failed to create Jenkins Organisation ConfigMap: " + e, e);
                        return Results.fail("Failed to create Jenkins Organisation ConfigMap:: " + e, e);
                    }
                }
            }
            // lets trigger the build
            Boolean triggerBuildFlag = triggerBuild.getValue();
            if (openShiftClient != null && triggerBuildFlag != null && triggerBuildFlag) {
                triggerBuild(openShiftClient, namespace, projectName);
            }

            for (String gitRepoName : gitRepoNameList) {
                try {
                    gitProvider.registerWebHook(details, new WebHookDetails(gitOwnerName, gitRepoName, webhookUrl, botSecret));
                    //registerGitWebHook(details, webhookUrl, gitOwnerName, gitRepoName, botSecret);
                } catch (Exception e) {
                    addWarning(warnings, "Failed to create CI webhooks for: " + gitRepoName + ": " + e, e);
                }
            }
            if (!gitRepoNameList.isEmpty()) {
                message += " and added git webhooks to repositories " + Strings.join(gitRepoNameList, ", ");
            }
            message += ". ";
        }
        String gitUrl = null;
        if (!gitRepos.isEmpty()) {
            gitUrl = gitRepos.get(0).getUrl();
        }
        CreateBuildConfigStatusDTO status = new CreateBuildConfigStatusDTO(namespace, projectName, gitUrl, cheStackId, jenkinsJobUrl, gitRepoNameList, gitRepos, gitOwnerName, warnings);
        return Results.success(message, status);
    }

    private List<GitRepoDTO> getGitRepos(UIContext uiContext, String gitRepoName) {
        List<GitRepoDTO> answer = new ArrayList<>();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();

        // lets handle repos from the ImportGit / GitCloneStep steps
        List<GitClonedRepoDetails> clonedRepos = (List<GitClonedRepoDetails>) attributeMap.get(AttributeMapKeys.GIT_CLONED_REPOS);
        if (clonedRepos != null) {
            for (GitClonedRepoDetails clonedRepo : clonedRepos) {
                addGitURl(answer, clonedRepo.getGitRepoName(), clonedRepo.getGitUrl());
            }
        }
        String gitUrl = (String) attributeMap.get(AttributeMapKeys.GIT_URL);
        addGitURl(answer, gitRepoName, gitUrl);
        return answer;
    }

    private static void addGitURl(List<GitRepoDTO> answer, String repoName, String gitUrl) {
        if (gitUrl != null) {
            for (GitRepoDTO repoDTO : answer) {
                if (gitUrl.equals(repoDTO.getUrl())) {
                    return;
                }
            }
            answer.add(new GitRepoDTO(repoName, gitUrl));
        }
    }


    private void ensureEnvVar(JenkinsPipelineBuildStrategy jenkinsPipelineStrategy, String envVar, String value) {
        List<EnvVar> env = jenkinsPipelineStrategy.getEnv();
        if (env == null) {
            env = new ArrayList<>();
        }
        for (EnvVar var : env) {
            if (envVar.equals(var.getName())) {
                return;
            }
        }
        env.add(new EnvVarBuilder().withName(envVar).withValue(value).build());
        jenkinsPipelineStrategy.setEnv(env);
    }


    private void ensureCDGihubSecretExists(KubernetesClient kubernetesClient, String namespace, String gitOwnerName, String gitToken) {
        String secretName = "cd-github";
        String username = Base64Helper.base64encode(gitOwnerName);
        String password = Base64Helper.base64encode(gitToken);
        Secret secret = null;
        Resource<Secret, DoneableSecret> secretResource = kubernetesClient.secrets().inNamespace(namespace).withName(secretName);
        try {
            secret = secretResource.get();
        } catch (Exception e) {
            LOG.warn("Failed to lookup secret " + namespace + "/" + secretName + " due to: " + e, e);
        }
        if (secret == null ||
                !Objects.equal(username, getSecretData(secret, "username")) ||
                !Objects.equal(password, getSecretData(secret, "password"))) {

            try {
                LOG.info("Upserting Secret " + namespace + "/" + secretName);
                secretResource.createOrReplace(new SecretBuilder().
                        withNewMetadata().withName(secretName).addToLabels("jenkins", "sync").addToLabels("creator", "fabric8").endMetadata().
                        addToData("username", username).
                        addToData("password", password).
                        build());
            } catch (Exception e) {
                LOG.warn("Failed to upsert Secret " + namespace + "/" + secretName + " due to: " + e, e);
            }
        }
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

    private void addWarning(List<String> warnings, String message, Exception e) {
        LOG.warn(message, e);
        // TODO add stack trace too?
        warnings.add(message);
    }

    protected void triggerBuild(OpenShiftClient openShiftClient, String namespace, String projectName) {
        for (int i = 0; i < retryTriggerBuildCount; i++) {
            if (i > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            String triggeredBuildName;
            BuildRequest request = new BuildRequestBuilder().
                    withNewMetadata().withName(projectName).endMetadata().
                    addNewTriggeredBy().withMessage("Forge triggered").endTriggeredBy().
                    build();
            try {
                Build build = openShiftClient.buildConfigs().inNamespace(namespace).withName(projectName).instantiate(request);
                if (build != null) {
                    triggeredBuildName = KubernetesHelper.getName(build);
                    LOG.info("Triggered build " + triggeredBuildName);
                    return;
                } else {
                    LOG.error("Failed to trigger build for " + namespace + "/" + projectName + " du to: no Build returned");
                }
            } catch (Exception e) {
                LOG.error("Failed to trigger build for " + namespace + "/" + projectName + " due to: " + e, e);
            }
        }
    }

    private void registerGitWebHook(GitAccount details, String webhookUrl, String gitOwnerName, String gitRepoName, String botSecret) throws IOException {

        // TODO move this logic into the GitProvider!!!
        String body = "{\"name\": \"web\",\"active\": true,\"events\": [\"*\"],\"config\": {\"url\": \"" + webhookUrl + "\",\"insecure_ssl\":\"1\"," +
                "\"content_type\": \"json\",\"secret\":\"" + botSecret + "\"}}";

        String authHeader = details.mandatoryAuthHeader();
        String createWebHookUrl = URLUtils.pathJoin("https://api.github.com/repos/", gitOwnerName, gitRepoName, "/hooks");

        // JAX-RS doesn't work so lets use trusty java.net.URL instead ;)
        HttpURLConnection connection = null;
        try {
            URL url = new URL(createWebHookUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
            connection.setRequestProperty("Authorization", authHeader);
            connection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream());
            out.write(body);

            out.close();
            int status = connection.getResponseCode();
            String message = connection.getResponseMessage();
            LOG.info("Got response code from github " + createWebHookUrl + " status: " + status + " message: " + message);
            if (status < 200 || status >= 300) {
                LOG.error("Failed to create the github web hook at: " + createWebHookUrl + ". Status: " + status + " message: " + message);
                throw new IllegalStateException("Failed to create the github web hook at: " + createWebHookUrl + ". Status: " + status + " message: " + message);
            }
        } catch (Exception e) {
            LOG.error("Failed to create the github web hook at: " + createWebHookUrl + ". " + e, e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Finds the secret token we should use for the web hooks
     */
    private String findBotSecret(String discoveryNamespace, String botServiceAccount) {
        if (useUiidForBotSecret) {
            return UUID.randomUUID().toString();
        } else {
            SecretList list = getKubernetesClientHelper().getKubernetesClient().secrets()
                    .inNamespace(discoveryNamespace).list();
            if (list != null) {
                List<Secret> items = list.getItems();
                if (items != null) {
                    for (Secret item : items) {
                        String name = KubernetesHelper.getName(item);
                        if (name.startsWith(botServiceAccount + "-token-")) {
                            Map<String, String> data = item.getData();
                            if (data != null) {
                                String token = data.get("token");
                                if (token != null) {
                                    return base64decode(token);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Triggers the given jenkins job via its URL.
     *
     * @param authHeader
     * @param jobUrl     the URL to the jenkins job
     * @param triggerUrl can be null or empty and the default triggerUrl will be used
     */
    protected void triggerJenkinsWebHook(String token, String authHeader, String jobUrl, String triggerUrl, boolean post) {
        if (Strings.isNullOrBlank(triggerUrl)) {
            //triggerUrl = URLUtils.pathJoin(jobUrl, "/build?token=" + token);
            triggerUrl = URLUtils.pathJoin(jobUrl, "/build?delay=0");
        }
        // lets check if this build is already running in which case do nothing
        String lastBuild = URLUtils.pathJoin(jobUrl, "/lastBuild/api/json");
        JsonNode lastBuildJson = parseLastBuildJson(authHeader, lastBuild);
        JsonNode building = null;
        if (lastBuildJson != null && lastBuildJson.isObject()) {
            building = lastBuildJson.get("building");
            if (building != null && building.isBoolean()) {
                if (building.booleanValue()) {
                    LOG.info("Build is already running so lets not trigger another one!");
                    return;
                }
            }
        }
        LOG.info("Got last build JSON: " + lastBuildJson + " building: " + building);

        LOG.info("Triggering Jenkins build: " + triggerUrl);

        Client client = WebClientHelpers.createClientWihtoutHostVerification();
        try {
            Response response = client.target(triggerUrl).
                    request().
                    header("Authorization", authHeader).
                    post(Entity.text(null), Response.class);

            int status = response.getStatus();
            String message = null;
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo != null) {
                message = statusInfo.getReasonPhrase();
            }
            String extra = "";
            if (status == 302) {
                extra = " Location: " + response.getLocation();
            }
            LOG.info("Got response code from Jenkins: " + status + " message: " + message + " from URL: " + triggerUrl + extra);
            if (status <= 200 || status > 302) {
                LOG.error("Failed to trigger job " + triggerUrl + ". Status: " + status + " message: " + message);
            }
        } finally {
            closeQuietly(client);
        }
    }

    protected JsonNode parseLastBuildJson(String authHeader, String urlText) {
        Client client = WebClientHelpers.createClientWihtoutHostVerification();
        try {
            Response response = client.target(urlText).
                    request().
                    header("Authorization", authHeader).
                    post(Entity.text(null), Response.class);

            int status = response.getStatus();
            String message = null;
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo != null) {
                message = statusInfo.getReasonPhrase();
            }
            LOG.info("Got response code from Jenkins: " + status + " message: " + message + " from URL: " + urlText);
            if (status <= 200 || status >= 300) {
                LOG.error("Failed to trigger job " + urlText + ". Status: " + status + " message: " + message);
            } else {
                String json = response.readEntity(String.class);
                if (Strings.isNotBlank(json)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        return objectMapper.reader().readTree(json);
                    } catch (IOException e) {
                        LOG.warn("Failed to parse JSON: " + e, e);
                    }
                }
            }
        } finally {
            closeQuietly(client);
        }
        return null;
    }

    private Response ensureJenkinsCDCredentialCreated(String gitUserName, String gitToken, String jenkinsUrl, String authHeader) {
        Client client = null;
        Response response = null;

        String createUrl = URLUtils.pathJoin(jenkinsUrl, "/credentials/store/system/domain/_/createCredentials");
        String getUrl = URLUtils.pathJoin(jenkinsUrl, "/credentials/store/system/domain/_/credentials/cd-github/");

        try {
            client = WebClientHelpers.createClientWihtoutHostVerification();
            response = client.target(getUrl).
                    request(MediaType.APPLICATION_JSON).
                    header("Authorization", authHeader).get(Response.class);

            int status = response.getStatus();
            String message = null;
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo != null) {
                message = statusInfo.getReasonPhrase();
            }
            String extra = "";
            if (status == 302) {
                extra = " Location: " + response.getLocation();
            }
            String resultText = convertEntityToText(response.getEntity());
            LOG.info("Got response code from Jenkins looking up credential: " + status + " message: " + message +
                             " from URL: " + getUrl + extra + " result: " + resultText);
            if (status >= 200 && status < 300) {
                return response;
            }
            if (status < 200 || status > 302) {
                LOG.error("Failed to lookup github credentials " + getUrl + ". Status: " + status + " message: " + message);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to lookup the github credentials in Jenkins at the URL " + getUrl + ". " + e, e);
        } finally {
            closeQuietly(client);
        }

        LOG.info("Creating Jenkins github credentials for github user name: " + gitUserName);


        String json = "{\n" +
                "  \"\": \"0\",\n" +
                "  \"credentials\": {\n" +
                "    \"scope\": \"GLOBAL\",\n" +
                "    \"id\": \"cd-github\",\n" +
                "    \"username\": \"" + gitUserName + "\",\n" +
                "    \"password\": \"" + gitToken + "\",\n" +
                "    \"description\": \"fabric8 CD credentials for github\",\n" +
                "    \"$class\": \"com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl\"\n" +
                "  }\n" +
                "}";

        Form form = new Form();
        form.param("json", json);

        try {
            client = WebClientHelpers.createClientWihtoutHostVerification();
            response = client.target(createUrl).request().
                    header("Authorization", authHeader).
                    post(Entity.form(form), Response.class);

            int status = response.getStatus();
            String message = null;
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo != null) {
                message = statusInfo.getReasonPhrase();
            }
            String extra = "";
            if (status == 302) {
                extra = " Location: " + response.getLocation();
            }
            LOG.info("Got response code from Jenkins: " + status + " message: " + message + " from URL: " + createUrl + extra);
            if (status < 200 || status > 302) {
                LOG.error("Failed to create credentials " + createUrl + ". Status: " + status + " message: " + message);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create the fabric8 credentials in Jenkins at the URL " + createUrl + ". " + e, e);
        } finally {
            closeQuietly(client);
        }
        return response;
    }

    private String convertEntityToText(Object entity) {
        try {
            if (entity instanceof InputStream) {
                return IOHelpers.readFully((InputStream) entity);
            } else if (entity instanceof Reader) {
                return IOHelpers.readFully((Reader) entity);
            } else if (entity != null) {
                return entity.toString();
            }
            return null;
        } catch (IOException e) {
            return "Failed to parse result: " + e;
        }
    }

    private ConfigMap ensureJenkinsCDOrganisationConfigMapCreated(KubernetesClient kubernetes, String namespace, String gitOwnerName, String gitRepoName) {

        String configMapName = KubernetesNames.convertToKubernetesName(gitOwnerName, false);
        Resource<ConfigMap, DoneableConfigMap> configMapResource = kubernetes.configMaps().inNamespace(namespace).withName(configMapName);
        ConfigMap cm = configMapResource.get();
        boolean update = true;
        if (cm == null) {
            update = false;
            cm = new ConfigMapBuilder().withNewMetadata().withName(configMapName).
                    addToLabels("provider", "fabric8").
                    addToLabels("openshift.io/jenkins", "job").endMetadata().withData(new HashMap<>()).build();
        }

        Map<String, String> data = cm.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(ConfigMapKeys.ROOT_JOB, "true");
        data.put(ConfigMapKeys.TRIGGER_ON_CHANGE, "true");

        String configXml = data.get(ConfigMapKeys.CONFIG_XML);
        Document document = null;
        if (Strings.isNotBlank(configXml)) {
            try {
                document = XmlUtils.parseDoc(new ByteArrayInputStream(configXml.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                LOG.warn("Could not parse current config.xml on " + namespace + "/" + configMapName + ". " + e, e);
            }
        }
        if (document == null || getGitHubScmNavigatorElement(document) == null) {
            document = parseGitHubOrgJobConfig();
            if (document == null) {
                throw new IllegalStateException("Cannot parse the template github org job XML!");
            }
        }
        setGitHubOrgJobOwnerAndRepo(document, gitOwnerName, gitRepoName);

        try {
            configXml = DomHelper.toXml(document);
        } catch (TransformerException e) {
            throw new IllegalStateException("Cannot convert the updated config.xml back to XML! " + e, e);
        }
        data.put(ConfigMapKeys.CONFIG_XML, configXml);

        if (update) {
            try {
                return configMapResource.edit().withData(data).done();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to update the Organisation Job ConfigMap " + namespace + "/" + configMapName + " due to: " + e, e);
            }
        } else {
            try {
                return configMapResource.create(cm);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create the Organisation Job ConfigMap " + namespace + "/" + configMapName + " due to: " + e, e);
            }
        }
    }

    private Response ensureJenkinsCDOrganisationJobCreated(String jenkinsUrl, String jobUrl, String oauthToken, String authHeader, String gitOwnerName, String gitRepoName) {
        String triggerUrl = URLUtils.pathJoin(jobUrl, "/build?delay=0");
        String getUrl = URLUtils.pathJoin(jobUrl, "/config.xml");
        String createUrl = URLUtils.pathJoin(jenkinsUrl, "/createItem?name=" + gitOwnerName);

        Document document = null;
        try {
            Response response = invokeRequestWithRedirectResponse(getUrl,
                                                                  target -> target.request(MediaType.TEXT_XML).
                                                                          header("Authorization", authHeader).
                                                                          get(Response.class));
            document = response.readEntity(Document.class);
            if (document == null) {
                document = parseEntityAsXml(response.readEntity(String.class));
            }
        } catch (Exception e) {
            LOG.warn("Failed to get github org job at " + getUrl + ". Probably does not exist? " + e, e);
        }

        boolean create = false;
        if (document == null || getGitHubScmNavigatorElement(document) == null) {
            create = true;
            document = parseGitHubOrgJobConfig();
            if (document == null) {
                throw new IllegalStateException("Cannot parse the template github org job XML!");
            }
        }

        setGitHubOrgJobOwnerAndRepo(document, gitOwnerName, gitRepoName);

        final Entity entity = Entity.entity(document, MediaType.TEXT_XML);
        Response answer;
        if (create) {
            try {
                answer = invokeRequestWithRedirectResponse(createUrl,
                                                           target -> target.request(MediaType.TEXT_XML).
                                                                   header("Authorization", authHeader).
                                                                   post(entity, Response.class));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create the GitHub Org Job at " + createUrl + ". " + e, e);
            }
        } else {
            try {
                answer = invokeRequestWithRedirectResponse(getUrl,
                                                           target -> target.request(MediaType.TEXT_XML).
                                                                   header("Authorization", authHeader).
                                                                   post(entity, Response.class));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to update the GitHub Org Job at " + getUrl + ". " + e, e);
            }
        }

        LOG.info("Triggering the job " + jobUrl);
        try {
            triggerJenkinsWebHook(oauthToken, authHeader, jobUrl, triggerUrl, true);
        } catch (Exception e) {
            LOG.error("Failed to trigger jenkins job at " + triggerUrl + ". " + e, e);
        }
        return answer;
    }

    private void setGitHubOrgJobOwnerAndRepo(Document doc, String gitOwnerName, String gitRepoName) {
        Element githubNavigator = getGitHubScmNavigatorElement(doc);
        if (githubNavigator == null) {
            throw new IllegalArgumentException("No element <" + GITHUB_SCM_NAVIGATOR_ELEMENT + "> found in the github organisation job!");
        }

        Element repoOwner = DomUtils.mandatoryFirstChild(githubNavigator, "repoOwner");
        Element pattern = DomHelper.firstChild(githubNavigator, "pattern");
        if (pattern == null) {
            // lets check for the new plugin XML
            Element traitsElement = DomHelper.firstChild(githubNavigator, "traits");
            if (traitsElement != null) {
                Element sourceFilterElement = DomHelper.firstChild(traitsElement, REGEX_SCM_SOURCE_FILTER_TRAIT_ELEMENT);
                if (sourceFilterElement != null) {
                    pattern = DomHelper.firstChild(sourceFilterElement, "regex");
                }
            }
        }
        if (pattern == null) {
            throw new IllegalArgumentException("No <pattern> or <traits><" + REGEX_SCM_SOURCE_FILTER_TRAIT_ELEMENT + "><regex> found in element <" + GITHUB_SCM_NAVIGATOR_ELEMENT + "> for the github organisation job!");
        }

        String newPattern = combineJobPattern(pattern.getTextContent(), gitRepoName);
        DomUtils.setElementText(repoOwner, gitOwnerName);
        DomUtils.setElementText(pattern, newPattern);
    }

    protected Element getGitHubScmNavigatorElement(Document doc) {
        Element githubNavigator = null;
        Element rootElement = doc.getDocumentElement();
        if (rootElement != null) {
            NodeList githubNavigators = rootElement.getElementsByTagName(GITHUB_SCM_NAVIGATOR_ELEMENT);
            for (int i = 0, size = githubNavigators.getLength(); i < size; i++) {
                Node item = githubNavigators.item(i);
                if (item instanceof Element) {
                    githubNavigator = (Element) item;
                    break;
                }
            }
        }
        return githubNavigator;
    }

    protected Response invokeRequestWithRedirectResponse(String url, Function<WebTarget, Response> callback) {
        boolean redirected = false;
        Response response = null;
        for (int i = 0, retries = 2; i < retries; i++) {
            Client client = null;
            try {
                client = WebClientHelpers.createClientWihtoutHostVerification();
                WebTarget target = client.target(url);
                response = callback.apply(target);
                int status = response.getStatus();
                String reasonPhrase = "";
                Response.StatusType statusInfo = response.getStatusInfo();
                if (statusInfo != null) {
                    reasonPhrase = statusInfo.getReasonPhrase();
                }
                LOG.info("Response from " + url + " is " + status + " " + reasonPhrase);
                if (status == 302) {
                    if (redirected) {
                        LOG.warn("Failed to process " + url + " and got status: " + status + " " + reasonPhrase, response);
                        throw new WebApplicationException("Failed to process " + url + " and got status: " + status + " " + reasonPhrase, response);
                    }
                    redirected = true;
                    URI uri = response.getLocation();
                    if (uri == null) {
                        LOG.warn("Failed to process " + url + " and got status: " + status + " " + reasonPhrase + " but no location header!", response);
                        throw new WebApplicationException("Failed to process " + url + " and got status: " + status + " " + reasonPhrase + " but no location header!", response);
                    }
                    url = uri.toString();
                } else if (status < 200 || status >= 300) {
                    LOG.warn("Failed to process " + url + " and got status: " + status + " " + reasonPhrase, response);
                    throw new WebApplicationException("Failed to process " + url + " and got status: " + status + " " + reasonPhrase, response);
                } else {
                    return response;
                }
            } catch (RedirectionException redirect) {
                if (redirected) {
                    throw redirect;
                }
                redirected = true;
                URI uri = redirect.getLocation();
                url = uri.toString();
            } finally {
                closeQuietly(client);
            }
        }
        return response;
    }

    private Client createSecureClient() {
        return ClientBuilder.newClient();
    }


    public KubernetesClientHelper getKubernetesClientHelper() {
        return kubernetesClientHelper;
    }

    private Document parseEntityAsXml(String entity) throws ParserConfigurationException, IOException, SAXException {
        if (entity == null) {
            return null;
        }
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(entity.getBytes()));
    }


    public Document parseGitHubOrgJobConfig() {
        String templateName = "github-org-job-config.xml";
        URL url = getClass().getResource(templateName);
        if (url == null) {
            LOG.error("Could not load " + templateName + " on the classpath!");
        } else {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                return documentBuilder.parse(url.toString());
            } catch (Exception e) {
                LOG.error("Failed to load template " + templateName + " from " + url + ". " + e, e);
            }
        }
        return null;
    }
}
