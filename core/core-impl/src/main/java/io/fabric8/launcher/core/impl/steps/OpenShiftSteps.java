package io.fabric8.launcher.core.impl.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;

import static io.fabric8.launcher.core.api.events.StatusEventType.OPENSHIFT_CREATE;
import static io.fabric8.launcher.core.api.events.StatusEventType.OPENSHIFT_PIPELINE;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class OpenShiftSteps {

    private static final Logger log = Logger.getLogger(OpenShiftSteps.class.getName());

    @Inject
    private OpenShiftService openShiftService;


    /**
     * Creates an Openshift project if the project doesn't exist.
     */
    public OpenShiftProject createOpenShiftProject(CreateProjectile projectile) {
        String projectName = projectile.getOpenShiftProjectName();
        OpenShiftProject openShiftProject = openShiftService.findProject(projectName)
                .orElseGet(() -> openShiftService.createProject(projectName));
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), OPENSHIFT_CREATE,
                                                                    singletonMap("location", openShiftProject.getConsoleOverviewUrl())));
        return openShiftProject;
    }

    public void configureBuildPipeline(CreateProjectile projectile, OpenShiftProject openShiftProject, GitRepository gitRepository) {
        File path = projectile.getProjectLocation().toFile();
        List<AppInfo> apps = findProjectApps(path);
        if (apps.isEmpty()) {
            // Use Jenkins pipeline build
            openShiftService.configureProject(openShiftProject, gitRepository.getGitCloneUri());
        } else {
            // Use S2I builder templates
            for (AppInfo app : apps) {
                for (File tpl : app.resources) {
                    applyTemplate(openShiftService, gitRepository, openShiftProject, app, tpl);
                }
            }
            for (AppInfo app : apps) {
                for (File tpl : app.services) {
                    applyTemplate(openShiftService, gitRepository, openShiftProject, app, tpl);
                }
            }
            for (AppInfo app : apps) {
                for (File tpl : app.apps) {
                    applyTemplate(openShiftService, gitRepository, openShiftProject, app, tpl);
                }
            }
        }

        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), OPENSHIFT_PIPELINE));
    }


    public List<URL> getWebhooks(OpenShiftProject project) {
        return openShiftService.getWebhookUrls(project);
    }

    private List<AppInfo> findProjectApps(File projectDir) {
        try (Stream<Path> stream = Files.walk(projectDir.toPath())) {
            return stream
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

    private void applyTemplate(OpenShiftService openShiftService, GitRepository gitHubRepository,
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
        final List<File> resources;

        final List<File> services;

        AppInfo(String contextDir, List<File> apps, List<File> resources, List<File> services) {
            this.contextDir = contextDir;
            this.apps = new ArrayList<>(apps);
            this.resources = new ArrayList<>(resources);
            this.services = new ArrayList<>(services);
        }

        final String contextDir;

        final List<File> apps;
    }
}
