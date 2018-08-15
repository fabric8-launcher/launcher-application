package io.fabric8.launcher.service.openshift.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.openshift.api.DuplicateProjectException;
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftResource;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.spi.OpenShiftServiceSpi;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildRequest;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import io.fabric8.openshift.api.model.DoneableTemplate;
import io.fabric8.openshift.api.model.Parameter;
import io.fabric8.openshift.api.model.ParameterBuilder;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.TemplateResource;

import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.stripEnd;

/**
 * Implementation of the {@link OpenShiftService} using the Fabric8
 * OpenShift client
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public final class Fabric8OpenShiftServiceImpl implements OpenShiftService, OpenShiftServiceSpi {

    private static final Logger log = Logger.getLogger(Fabric8OpenShiftServiceImpl.class.getName());

    private static final Pattern PARAM_VAR_PATTERN = Pattern.compile("\\{\\{(.*?)/(.*?)\\[(.*)\\]\\}\\}");

    static {
        // Avoid using ~/.kube/config
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
        // Avoid using /var/run/secrets/kubernetes.io/serviceaccount/token
        System.setProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY, "false");
    }

    private final OpenShiftClient client;

    private final URL consoleUrl;

    /**
     * Creates an {@link OpenShiftService} implementation communicating
     * with the backend service via the specified parameters
     *
     * @param parameterse \
     */
    Fabric8OpenShiftServiceImpl(final OpenShiftParameters parameters) {
        OpenShiftCluster cluster = parameters.getCluster();
        Identity identity = parameters.getIdentity();
        try {
            this.consoleUrl = new URL(cluster.getConsoleUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(cluster.getApiUrl())
                //TODO Issue #17 never do this in production as it opens us to man-in-the-middle attacks
                .withTrustCerts(true);
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                configBuilder.withOauthToken(token.getToken());
            }

            @Override
            public void visit(UserPasswordIdentity userPassword) {
                configBuilder
                        .withUsername(userPassword.getUsername())
                        .withPassword(userPassword.getPassword());
            }
        });
        final Config config = configBuilder.build();
        String impersonateUsername = parameters.getImpersonateUsername();
        if (impersonateUsername != null) {
            // Impersonate the given user name (can be null)
            RequestConfig requestConfig = config.getRequestConfig();
            requestConfig.setImpersonateUsername(impersonateUsername);
            requestConfig.setImpersonateGroups("system:authenticated","system:authenticated:oauth");
        }
        this.client = new DefaultOpenShiftClient(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenShiftProject createProject(final String name) throws
            DuplicateProjectException,
            IllegalArgumentException {

        // Create
        final ProjectRequest projectRequest;
        try {
            projectRequest = client.projectrequests().createNew().
                    withNewMetadata().
                    withName(name).
                    endMetadata().
                    done();
        } catch (final KubernetesClientException kce) {
            throw ExceptionMapper.throwMappedException(kce, name);
        }

        // Block until exists
        int counter = 0;
        while (true) {
            counter++;
            if (projectExists(name)) {
                // We good
                break;
            }
            if (counter == 10) {
                throw new IllegalStateException("Newly-created project "
                                                        + name + " could not be found ");
            }
            log.log(FINEST, "Could not find project {0} after creating; waiting and trying again...", name);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException ie) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                throw new RuntimeException("Someone interrupted thread while finding newly-created project", ie);
            }
        }
        // Populate value object and return it
        final String roundtripDisplayName = projectRequest.getMetadata().getName();

        return new OpenShiftProjectImpl(roundtripDisplayName, consoleUrl.toString());
    }

    @Override
    public Optional<OpenShiftProject> findProject(String name) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        return Optional.ofNullable(projectExists(name) ? new OpenShiftProjectImpl(name, consoleUrl.toString()) : null);
    }

    @Override
    public void configureProject(final OpenShiftProject project, final URI sourceRepositoryUri) {
        final InputStream pipelineTemplateStream = getClass().getResourceAsStream("/pipeline-template.yml");
        List<Parameter> parameters = getParameters(project, sourceRepositoryUri, null);
        configureProject(project, pipelineTemplateStream, parameters);
        fixJenkinsServiceAccount(project);
    }

    @Override
    public void configureProject(final OpenShiftProject project, InputStream templateStream, final URI sourceRepositoryUri, final String sourceRepositoryContextDir) {
        List<Parameter> parameters = getParameters(project, sourceRepositoryUri, sourceRepositoryContextDir);
        configureProject(project, templateStream, parameters);
    }

    private List<Parameter> getParameters(OpenShiftProject project, URI sourceRepositoryUri, @Nullable String sourceRepositoryContextDir) {
        List<Parameter> parameters = new ArrayList<>();
        String repositoryName = getRepositoryName(sourceRepositoryUri);
        if (isNotBlank(repositoryName)) {
            parameters.add(createParameter("SOURCE_REPOSITORY_NAME", repositoryName));
        }
        parameters.add(createParameter("SOURCE_REPOSITORY_URL", sourceRepositoryUri.toString()));
        if (sourceRepositoryContextDir != null) {
            parameters.add(createParameter("SOURCE_REPOSITORY_DIR", sourceRepositoryContextDir));
        }
        parameters.add(createParameter("PROJECT", project.getName()));
        parameters.add(createParameter("OPENSHIFT_CONSOLE_URL", this.getConsoleUrl().toString()));
        parameters.add(createParameter("GITHUB_WEBHOOK_SECRET", Long.toString(System.currentTimeMillis())));
        return parameters;
    }


    @Override
    public void configureProject(OpenShiftProject project, InputStream templateStream, Map<String, String> parameters) {
        requireNonNull(project, "Project cannot be null");
        requireNonNull(templateStream, "Template cannot be null");
        requireNonNull(parameters, "Parameters cannot be null");
        List<Parameter> parameterList = parameters.entrySet().stream()
                .map(e -> createParameter(e.getKey(), e.getValue())).collect(Collectors.toList());
        configureProject(project, templateStream, parameterList);
    }

    /**
     * Extract the Git Repository name to be used in the SOURCE_REPOSITORY_NAME parameter in the templates
     *
     * @param uri a GitHub URI (eg https://github.com/foo/bar)
     * @return the repository name of the given {@link URI} (eg. bar)
     */
    static String getRepositoryName(URI uri) {
        String path = stripEnd(uri.getPath(), "/");
        String substring = path.substring(path.lastIndexOf('/') + 1);
        return stripEnd(substring, ".git");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<URL> getWebhookUrls(final OpenShiftProject project) {
        if (project == null) {
            throw new IllegalArgumentException("project must be specified");
        }
        final URL openshiftConsoleUrl = this.getConsoleUrl();
        return project.getResources().stream()
                .filter(r -> "BuildConfig".equals(r.getKind()))
                .map(buildConfig -> {
                    // Construct a URL in form:
                    // https://<OS_IP>:<OS_PORT>/oapi/v1/namespaces/<project>/buildconfigs/<BC-name/webhooks/<secret>/github
                    final String secret = buildConfig.getGitHubWebhookSecret();
                    final String webhookContext = "/oapi/v1/namespaces/" +
                            project.getName() + "/buildconfigs/" +
                            buildConfig.getName() + "/webhooks/" + secret + "/github";
                    try {
                        return new URL(openshiftConsoleUrl.getProtocol(), openshiftConsoleUrl.getHost(),
                                       openshiftConsoleUrl.getPort(), webhookContext);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Failed to create Webhook URL for project '" + project.getName()
                                                           + "' using the OpenShift API URL '" + openshiftConsoleUrl.toExternalForm()
                                                           + "' and the webhook context '" + webhookContext + "'", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(final OpenShiftProject project) {
        if (project == null) {
            throw new IllegalArgumentException("project must be specified");
        }
        final String projectName = project.getName();
        return this.deleteProject(projectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(final String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("project name must be specified");
        }
        final boolean deleted = client.projects().withName(projectName).delete();
        if (deleted) {
            log.log(FINEST, "Deleted project: {0}", projectName);
        }
        return deleted;
    }

    @Override
    public boolean projectExists(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        try {
            Project project = client.projects().withName(name).get();
            return project != null;
        } catch (KubernetesClientException ignored) {
            return false;
        }
    }

    @Override
    public URL getServiceURL(String serviceName, OpenShiftProject project) {
        String serviceURL = null;
        try {
            serviceURL = client.services()
                    .inNamespace(project.getName())
                    .withName(serviceName)
                    .getURL("");
            return serviceURL == null ? null : new URL(serviceURL.replace("tcp://", "https://"));
        } catch (KubernetesClientException e) {

            throw new IllegalArgumentException("Service does not exist: " + serviceName, e);
        } catch (MalformedURLException e) {
            // Should never happen
            throw new IllegalStateException("Malformed service URL: " + serviceURL, e);
        }
    }

    private Parameter createParameter(final String name, final String value) {
        return new ParameterBuilder().withName(name).withValue(value).build();
    }

    private void configureProject(final OpenShiftProject project, final InputStream templateStream,
                                  List<Parameter> parameters) {
        try {
            try (final InputStream pipelineTemplateStream = templateStream) {
                TemplateResource<Template, KubernetesList, DoneableTemplate> templateResource = client.templates()
                        .inNamespace(project.getName()).load(pipelineTemplateStream);
                Map<String, String> parameterValues = applyParameterValueProperties(project, parameters)
                        .stream()
                        .collect(toMap(Parameter::getName, Parameter::getValue));
                if (log.isLoggable(Level.FINEST)) {
                    parameterValues.forEach((key, value) -> log.finest("\t" + key + '=' + value));
                }
                final KubernetesList processedTemplate = templateResource.process(parameterValues);
                processedTemplate.getItems().stream()
                        .map(item -> {
                            // Create resource
                            client.resource(item).inNamespace(project.getName()).createOrReplace();
                            String gitHubWebHookSecret = null;
                            if (item instanceof BuildConfig) {
                                final BuildConfig bc = (BuildConfig) item;
                                gitHubWebHookSecret = bc.getSpec().getTriggers()
                                        .stream()
                                        .filter(r -> r.getGithub() != null)
                                        .map(r -> r.getGithub().getSecret())
                                        .findFirst().orElse(null);
                            }
                            return ImmutableOpenShiftResource.builder()
                                    .name(item.getMetadata().getName())
                                    .kind(item.getKind())
                                    .project(project)
                                    .gitHubWebhookSecret(gitHubWebHookSecret)
                                    .build();
                        })
                        .forEach(resource -> {
                            log.finest("Adding resource '" + resource.getName() + "' (" + resource.getKind()
                                               + ") to project '" + project.getName() + "'");
                            ((OpenShiftProjectImpl) project).addResource(resource);
                        });
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create OpenShift pipeline", e);
        }
    }

    // This function looks for any parameters in the template that have the special
    // property "fabric8-value". If it encounters one it will take the property's
    // value string and look for any variables to replace.
    // The variables have the format <code>{{TYPE/NAME[PROPERTY_PATH]}}</code> where
    // <code>TYPE</code> is an OpenShift object type (like "route") and <code>NAME</code>
    // is the name of the object to look for and <code>PROPERTY_PATH</code> is the
    // property to get from that object (eg ".spec.host").
    // The variable will be replaced with the value obtained from the indicated
    // object property.
    private List<Parameter> applyParameterValueProperties(final OpenShiftProject project, List<Parameter> parameters) {
        RouteList routes = client.routes().inNamespace(project.getName()).list();
        return parameters.stream()
                .map(p -> new ParameterBuilder(p)
                        .withValue(replaceParameterVariable(p, routes))
                        .build()).collect(Collectors.toList());
    }

    private String replaceParameterVariable(Parameter p, RouteList routes) {
        // Find any parameters with special "fabric8-value" properties
        if (p.getAdditionalProperties().containsKey("fabric8-value")
                && p.getValue() == null) {
            String value = p.getAdditionalProperties().get("fabric8-value").toString();
            Matcher m = PARAM_VAR_PATTERN.matcher(value);
            StringBuffer newval = new StringBuffer();
            while (m.find()) {
                String type = m.group(1);
                String routeName = m.group(2);
                String propertyPath = m.group(3);
                String propertyValue = "";
                // We only support "route/XXX[.spec.host]" for now,
                // but we're prepared for future expansion
                if ("route".equals(type) && ".spec.host".equals(propertyPath)) {
                    propertyValue = routes.getItems().stream()
                            .filter(r -> routeName.equals(r.getMetadata().getName()))
                            .map(r -> r.getSpec().getHost())
                            .filter(Objects::nonNull)
                            .findAny()
                            .orElse(propertyValue);
                }
                m.appendReplacement(newval, Matcher.quoteReplacement(propertyValue));
            }
            m.appendTail(newval);
            return newval.toString();
        } else {
            return p.getValue();
        }
    }

    private void fixJenkinsServiceAccount(final OpenShiftProject project) {
        // Add Admin role to the jenkins serviceaccount
        log.finest(() -> "Adding role admin to jenkins serviceaccount for project '" + project.getName() + "'");
        client.roleBindings()
                .inNamespace(project.getName())
                .withName("admin")
                .edit()
                .addToUserNames("system:serviceaccount:" + project.getName() + ":jenkins")
                .addNewSubject().withKind("ServiceAccount").withNamespace(project.getName()).withName("jenkins").endSubject()
                .done();

    }

    private URL getConsoleUrl() {
        return consoleUrl;
    }

    @Override
    public OpenShiftClient getOpenShiftClient() {
        return client;
    }

    @Override
    public void applyBuildConfig(BuildConfig buildConfig, String namespace, String sourceName) {
        client.resource(buildConfig).inNamespace(namespace).createOrReplace();
    }

    @Override
    public void deleteBuildConfig(final String namespace, final String name) {
        client.buildConfigs().inNamespace(namespace).withName(name).delete();
    }

    @Override
    public Optional<ConfigMap> getConfigMap(String configName, String namespace) {
        return Optional.ofNullable(getResource(configName, namespace).get());
    }

    @Override
    public void createConfigMap(String configName, String namespace, ConfigMap configMap) {
        getResource(configName, namespace).create(configMap);
    }

    @Override
    public void updateConfigMap(String configName, String namespace, Map<String, String> data) {
        getResource(configName, namespace).edit().withData(data).done();
    }

    @Override
    public void deleteConfigMap(final String namespace, final String configName) {
        getResource(configName, namespace).delete();
    }

    @Override
    public void triggerBuild(String projectName, String namespace) {
        String triggeredBuildName;
        BuildRequest request = new BuildRequestBuilder().
                withNewMetadata().withName(projectName).endMetadata().
                addNewTriggeredBy().withMessage("Forge triggered").endTriggeredBy().
                build();
        try {
            Build build = client.buildConfigs().inNamespace(namespace)
                    .withName(projectName).instantiate(request);
            if (build != null) {
                triggeredBuildName = build.getMetadata().getName();
                log.info("Triggered build " + triggeredBuildName);
            } else {
                log.severe("Failed to trigger build for " + namespace + "/" + projectName + " due to: no Build returned");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to trigger build for " + namespace + "/" + projectName + " due to: " + e, e);
        }
    }

    @Override
    public ConfigMap createNewConfigMap(String ownerName) {
        String configMapName = convertToKubernetesName(ownerName, false);
        return new ConfigMapBuilder().withNewMetadata().withName(configMapName).
                addToLabels("provider", "fabric8").
                addToLabels("openshift.io/jenkins", "job").endMetadata().withData(new HashMap<>()).build();
    }

    private Resource<ConfigMap, DoneableConfigMap> getResource(String configName, String namespace) {
        String configMapName = convertToKubernetesName(configName, false);
        return client.configMaps().inNamespace(namespace).withName(configMapName);

    }

    private static String convertToKubernetesName(String text, boolean allowDots) {
        String lower = text.toLowerCase();
        StringBuilder builder = new StringBuilder();
        boolean started = false;
        char lastCh = ' ';
        for (int i = 0, last = lower.length() - 1; i <= last; i++) {
            char ch = lower.charAt(i);
            boolean digit = ch >= '0' && ch <= '9';
            // names cannot start with a digit so lets add a prefix
            if (digit && builder.length() == 0) {
                builder.append('n');
            }
            if (!(ch >= 'a' && ch <= 'z') && !digit) {
                if (ch == '/') {
                    ch = '.';
                } else if (ch != '.' && ch != '-') {
                    ch = '-';
                }
                if (!allowDots && ch == '.') {
                    ch = '-';
                }
                if (!started || lastCh == '-' || lastCh == '.' || i == last) {
                    continue;
                }
            }
            builder.append(ch);
            started = true;
            lastCh = ch;
        }
        return builder.toString();
    }
}
