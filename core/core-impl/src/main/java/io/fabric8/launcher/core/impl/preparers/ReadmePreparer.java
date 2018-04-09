package io.fabric8.launcher.core.impl.preparers;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.CreateProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;
import io.fabric8.launcher.core.api.documentation.BoosterReadmeProcessor;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

@ApplicationScoped
public class ReadmePreparer implements ProjectilePreparer {

    private static final Logger LOG = Logger.getLogger(ReadmePreparer.class.getName());

    private final BoosterReadmeProcessor readmeProcessor;

    @Inject
    public ReadmePreparer(final BoosterReadmeProcessor readmeProcessor) {
        this.readmeProcessor = Objects.requireNonNull(readmeProcessor, "readmeProcessor must be specified.");
    }

    @Override
    public void prepare(final Path projectPath, final RhoarBooster booster, final ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            return;
        }
        CreateProjectileContext createProjectileContext = (CreateProjectileContext) context;
        try {
            // Create README.adoc file
            String template = readmeProcessor.getReadmeTemplate(createProjectileContext.getMission());
            if (template != null) {
                Map<String, String> values = new HashMap<>();
                values.put("missionId", createProjectileContext.getMission().getId());
                values.put("mission", createProjectileContext.getMission().getName());
                values.put("runtimeId", createProjectileContext.getRuntime().getId());
                values.put("runtime", createProjectileContext.getRuntime().getName());
                if (createProjectileContext.getRuntimeVersion() != null) {
                    values.put("runtimeVersion", createProjectileContext.getRuntimeVersion().getName());
                } else {
                    values.put("runtimeVersion", "");
                }
                values.put("groupId", createProjectileContext.getGroupId());
                values.put("artifactId", createProjectileContext.getArtifactId());
                values.put("version", createProjectileContext.getProjectVersion());
                String deploymentType = "zip";
                if (context instanceof LauncherProjectileContext) {
                    LauncherProjectileContext createContext = (LauncherProjectileContext) context;
                    values.put("openShiftProject", createContext.getProjectName());
                    values.put("targetRepository", Objects.toString(createContext.getGitRepository(), createContext.getProjectName()));
                    deploymentType = "cd";
                }
                values.putAll(readmeProcessor.getRuntimeProperties(deploymentType, createProjectileContext.getMission(), createProjectileContext.getRuntime()));
                String readmeOutput = readmeProcessor.processTemplate(template, values);
                // Write README.adoc
                Files.write(projectPath.resolve("README.adoc"), readmeOutput.getBytes());
                // Delete README.md
                Files.deleteIfExists(projectPath.resolve("README.md"));
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                LOG.log(Level.WARNING, "No README.adoc and properties found for " + createProjectileContext.getMission().getId() + " " + createProjectileContext.getRuntime().getId() +
                        ". Check to see if there is a corresponding properties file for your Mission, Runtime, and DeploymentType here: " +
                        "https://github.com/fabric8-launcher/launcher-documentation/tree/master/docs/topics/readme");

            } else {
                LOG.log(Level.SEVERE, "Error while creating README.adoc", e);
            }
        }
    }

}
