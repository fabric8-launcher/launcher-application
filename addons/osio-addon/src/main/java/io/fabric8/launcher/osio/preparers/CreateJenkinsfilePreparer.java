package io.fabric8.launcher.osio.preparers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.osio.jenkins.JenkinsPipeline;
import io.fabric8.launcher.osio.jenkins.JenkinsPipelineRegistry;
import io.fabric8.launcher.osio.projectiles.context.OsioImportProjectileContext;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class CreateJenkinsfilePreparer implements ProjectilePreparer {

    @Inject
    private JenkinsPipelineRegistry pipelineRegistry;

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext genericContext) {
        if (!(genericContext instanceof OsioImportProjectileContext)) {
            return;
        }
        OsioImportProjectileContext context = (OsioImportProjectileContext) genericContext;
        JenkinsPipeline jenkinsPipeline = pipelineRegistry.findPipelineById(context.getPipelineId())
                .orElseThrow(() -> new IllegalArgumentException("Pipeline Id not found: " + context.getPipelineId()));
        Path jenkinsfilePath = jenkinsPipeline.getJenkinsfilePath();
        try {
            Files.copy(jenkinsfilePath, projectPath.resolve(jenkinsfilePath.getFileName()), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot copy Jenkinsfile from selected pipeline", e);
        }
    }
}
