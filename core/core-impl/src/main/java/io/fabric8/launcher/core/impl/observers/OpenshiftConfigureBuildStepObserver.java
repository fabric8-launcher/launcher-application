package io.fabric8.launcher.core.impl.observers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.StatusMessageEvent;
import io.fabric8.launcher.core.api.inject.Step;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.core.api.StatusEventType.OPENSHIFT_PIPELINE;

/**
 * Setup build either using s2i or jenkins pipeline.
 */
@ApplicationScoped
public class OpenshiftConfigureBuildStepObserver {

    private Logger log = Logger.getLogger(OpenshiftConfigureBuildStepObserver.class.getName());

    private final OpenShiftServiceFactory openShiftServiceFactory;

    private final OpenShiftClusterRegistry openShiftClusterRegistry;

    private final GitHubServiceFactory gitHubServiceFactory;

    private final Event<StatusMessageEvent> statusEvent;

    @Inject
    public OpenshiftConfigureBuildStepObserver(OpenShiftServiceFactory openShiftServiceFactory,
                                               OpenShiftClusterRegistry openShiftClusterRegistry,
                                               GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
        this.openShiftServiceFactory = openShiftServiceFactory;
        this.openShiftClusterRegistry = openShiftClusterRegistry;
        this.gitHubServiceFactory = gitHubServiceFactory;
    }


    public void execute(@Observes @Step(OPENSHIFT_PIPELINE)CreateProjectile projectile) {
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());

        OpenShiftProject openShiftProject = openShiftService.findProject(projectile.getOpenShiftProjectName()).get();
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.getRepository(projectile.getGitHubRepositoryName());

        File path = projectile.getProjectLocation().toFile();
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

        statusEvent.fire(new StatusMessageEvent(projectile.getId(), OPENSHIFT_PIPELINE));
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

    private class AppInfo {
        final String contextDir;

        final List<File> apps;

        public final List<File> resources;

        public final List<File> services;

        AppInfo(String contextDir, List<File> apps, List<File> resources, List<File> services) {
            this.contextDir = contextDir;
            this.apps = new ArrayList<>(apps);
            this.resources = new ArrayList<>(resources);
            this.services = new ArrayList<>(services);
        }
    }
}
