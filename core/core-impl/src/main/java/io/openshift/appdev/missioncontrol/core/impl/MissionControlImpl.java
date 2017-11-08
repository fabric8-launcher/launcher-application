package io.openshift.appdev.missioncontrol.core.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.TokenIdentity;
import io.openshift.appdev.missioncontrol.core.api.Boom;
import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.ForkProjectile;
import io.openshift.appdev.missioncontrol.core.api.LaunchEvent;
import io.openshift.appdev.missioncontrol.core.api.MissionControl;
import io.openshift.appdev.missioncontrol.core.api.Projectile;
import io.openshift.appdev.missioncontrol.core.api.StatusMessage;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.service.github.api.DuplicateWebhookException;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhookEvent;
import io.openshift.appdev.missioncontrol.service.github.spi.GitHubServiceSpi;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;
import org.apache.commons.lang.text.StrSubstitutor;

import static java.util.Collections.singletonMap;

/**
 * Implementation of the {@link MissionControl} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class MissionControlImpl implements MissionControl {

    private static final Logger log = Logger.getLogger(MissionControlImpl.class.getName());

    private static final String LOCAL_USER_ID_PREFIX = "LOCAL_USER_";

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry openShiftClusterRegistry;

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private Event<StatusMessageEvent> statusEvent;

    @Inject
    private Event<LaunchEvent> launchEvent;

    /**
     * {@inheritDoc}
     */
    @Override
    public Boom launch(final ForkProjectile projectile) throws IllegalArgumentException {

        final GitHubService gitHubService = getGitHubService(projectile);
        GitHubRepository gitHubRepository;
        // Get properties
        final String sourceRepoName = projectile.getSourceGitHubRepo();
        gitHubRepository = gitHubService.fork(sourceRepoName);

        //TODO
        // https://github.com/redhat-kontinuity/catapult/issues/18
        // Create a new OpenShift project for the user
        final String projectName = projectile.getOpenShiftProjectName();

        /*
          TODO Figure how to best handle possible DuplicateProjectException, but has to be handled to the user at some intelligent level
         */
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        assert cluster.isPresent() : "OpenShift Cluster not found: " + projectile.getOpenShiftClusterName();
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());
        final OpenShiftProject createdProject = openShiftService.createProject(projectName);

        /*
         * Construct the full URI for the pipeline template file,
         * relative to the repository root
         */
        final URI pipelineTemplateUri = UriBuilder.fromUri("https://raw.githubusercontent.com/")
                .path(projectile.getSourceGitHubRepo())
                .path(projectile.getGitRef())
                .path(projectile.getPipelineTemplatePath()).build();

        // Configure the OpenShift project
        openShiftService.configureProject(createdProject,
                                          gitHubRepository.getGitCloneUri(),
                                          projectile.getGitRef(),
                                          pipelineTemplateUri);

        List<GitHubWebhook> webhooks = getGitHubWebhooks(gitHubService, openShiftService, gitHubRepository, createdProject);

        // Return information needed to continue flow to the user
        return new BoomImpl(gitHubRepository, createdProject, webhooks);
    }

    @Override
    public Boom launch(CreateProjectile projectile) throws IllegalArgumentException {
        final GitHubService gitHubService = getGitHubService(projectile);
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        assert cluster.isPresent() : "OpenShift Cluster not found: " + projectile.getOpenShiftClusterName();
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());

        String projectName = projectile.getOpenShiftProjectName();
        File path = projectile.getProjectLocation().toFile();

        String repositoryName = projectile.getGitHubRepositoryName();
        if (repositoryName == null) {
            repositoryName = projectName;
        }
        String repositoryDescription = projectile.getGitHubRepositoryDescription();
        GitHubRepository gitHubRepository = gitHubService.createRepository(repositoryName, repositoryDescription);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), StatusMessage.GITHUB_CREATE, singletonMap("location", gitHubRepository.getHomepage())));

        // Add logged user in README.adoc
        File readmeAdoc = new File(path, "README.adoc");
        if (readmeAdoc.exists()) {
            try {
                String content = new String(Files.readAllBytes(readmeAdoc.toPath()));
                Map<String, String> values = new HashMap<>();
                values.put("loggedUser", gitHubService.getLoggedUser().getLogin());
                String newContent = new StrSubstitutor(values).replace(content);
                Files.write(readmeAdoc.toPath(), newContent.getBytes());
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error while replacing README.adoc variables", e);
            }
        }

        gitHubService.push(gitHubRepository, path);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), StatusMessage.GITHUB_PUSHED));

        OpenShiftProject openShiftProject = openShiftService.findProject(projectName).orElseGet(() -> openShiftService.createProject(projectName));
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), StatusMessage.OPENSHIFT_CREATE, singletonMap("location", openShiftProject.getConsoleOverviewUrl())));

        List<AppInfo> apps = findProjectApps(path);
        if (apps.isEmpty()) {
            // Use Jenkins pipeline build
            openShiftService.configureProject(openShiftProject, gitHubRepository.getGitCloneUri());
        } else {
            // Use S2I builder templates
            for (AppInfo app : apps) {
                for (File tpl : app.resources) {
                    applyTemplate(openShiftService, gitHubRepository, openShiftProject, app, tpl);
                }
            }
            for (AppInfo app : apps) {
                for (File tpl : app.services) {
                    applyTemplate(openShiftService, gitHubRepository, openShiftProject, app, tpl);
                }
            }
            for (AppInfo app : apps) {
                for (File tpl : app.apps) {
                    applyTemplate(openShiftService, gitHubRepository, openShiftProject, app, tpl);
                }
            }
        }
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), StatusMessage.OPENSHIFT_PIPELINE));

        List<GitHubWebhook> webhooks = getGitHubWebhooks(gitHubService, openShiftService, gitHubRepository, openShiftProject);
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), StatusMessage.GITHUB_WEBHOOK));
        launchEvent.fire(new LaunchEvent(getUserId(projectile), projectile.getId(), gitHubRepository.getFullName(), openShiftProject.getName(), projectile.getMission(), projectile.getRuntime()));
        return new BoomImpl(gitHubRepository, openShiftProject, webhooks);
    }

    private void applyTemplate(OpenShiftService openShiftService, GitHubRepository gitHubRepository,
            OpenShiftProject openShiftProject, AppInfo app, File tpl) {
        try (FileInputStream fis = new FileInputStream(tpl)) {
            openShiftService.configureProject(openShiftProject, fis, gitHubRepository.getGitCloneUri(), app.contextDir);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Could not apply services template", e);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read services template", e);
        }
    }

    private String getUserId(Projectile projectile) {
        final Identity identity = projectile.getOpenShiftIdentity();
        String userId;
        // User ID will be the token
        if (identity instanceof TokenIdentity) {
            userId = ((TokenIdentity) identity).getToken();
        } else {
            // For users authenticating with user/password (ie. local/Minishift/CDK)
            // let's identify them by their MAC address (which in a VM is the MAC address
            // of the VM, or a fake one, but all we can really rely on to uniquely identify
            // an installation
            final StringBuilder sb = new StringBuilder();
            try {
                byte[] macAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
                sb.append(LOCAL_USER_ID_PREFIX);
                for (int i = 0; i < macAddress.length; i++) {
                    sb.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
                }
                userId = sb.toString();
            } catch (Exception e) {
                userId = LOCAL_USER_ID_PREFIX + "UNKNOWN";
            }
        }
        return userId;
    }

    private List<GitHubWebhook> getGitHubWebhooks(GitHubService gitHubService, OpenShiftService openShiftService,
                                                  GitHubRepository gitHubRepository, OpenShiftProject createdProject) {
        List<GitHubWebhook> webhooks = openShiftService.getWebhookUrls(createdProject).stream()
                .map(webhookUrl -> {
                    try {
                        return gitHubService.createWebhook(gitHubRepository, webhookUrl, GitHubWebhookEvent.PUSH);
                    } catch (final DuplicateWebhookException dpe) {
                        // Swallow, it's OK, we've already forked this repo
                        log.log(Level.INFO, dpe.getMessage());
                        return ((GitHubServiceSpi) gitHubService).getWebhook(gitHubRepository, webhookUrl);
                    }
                })
                .collect(Collectors.toList());
        return webhooks;
    }

    private GitHubService getGitHubService(Projectile projectile) {
        if (projectile == null) {
            throw new IllegalArgumentException("projectile must be specified");
        }
        return gitHubServiceFactory.create(projectile.getGitHubIdentity());
    }

    private List<AppInfo> findProjectApps(File projectDir) {
        try {
            return Files.walk(projectDir.toPath())
                    .map(Path::toFile)
                    .filter(file -> file.isDirectory()
                            && file.getName().equals(".openshiftio"))
                    .map(file -> {
                        File contextDir = getContextDir(file.getParentFile(), projectDir);
                        List<File> resources = listYamlFiles(file, "resource.");
                        List<File> services = listYamlFiles(file, "service.");
                        List<File> apps = listYamlFiles(file, "application.");
                        boolean hasTemplates = !resources.isEmpty() || !services.isEmpty() || !apps.isEmpty();
                        if (contextDir != null && !contextDir.toString().isEmpty() && hasTemplates) {
                            return new AppInfo(contextDir.toString(), apps, resources, services);
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while finding project applications", e);
            return Collections.emptyList();
        }
    }

    private File getContextDir(File dir, File rootDir) {
        File rel = rootDir.toPath().relativize(dir.toPath()).toFile();
        if (!rel.toString().isEmpty()) {
            return rel;
        } else {
            return new File(".");
        }
    }

    private List<File> listYamlFiles(File dir, String prefix) {
        File[] ymls = dir.listFiles(f -> {
            String name = f.getName();
            return name.startsWith(prefix)
                    && (name.endsWith(".yml") || name.endsWith(".yaml"));
        });
        return ymls != null ? Arrays.asList(ymls) : Collections.emptyList();
    }

    private class AppInfo {
        public final String contextDir;

        public final List<File> apps;
        public final List<File> resources;
        public final List<File> services;

        public AppInfo(String contextDir, List<File> apps, List<File> resources, List<File> services) {
            this.contextDir = contextDir;
            this.apps = new ArrayList<>(apps);
            this.resources = new ArrayList<>(resources);
            this.services = new ArrayList<>(services);
        }
    }

}
