package io.fabric8.launcher.osio.preparers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.osio.jenkins.JenkinsPipeline;
import io.fabric8.launcher.osio.jenkins.JenkinsPipelineRegistry;
import io.fabric8.launcher.osio.projectiles.OsioProjectileContext;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class CreateJenkinsfilePreparer implements ProjectilePreparer {
    private static final Pattern INJECT_FRAGMENT = Pattern.compile("// INJECT FRAGMENT (.*)");

    @Inject
    JenkinsPipelineRegistry pipelineRegistry;

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext genericContext) {
        if (!(genericContext instanceof OsioProjectileContext)) {
            return;
        }
        OsioProjectileContext context = (OsioProjectileContext) genericContext;
        JenkinsPipeline jenkinsPipeline = pipelineRegistry.findPipelineById(context.getPipelineId())
                .orElseThrow(() -> new IllegalArgumentException("Pipeline Id not found: " + context.getPipelineId()));
        Path jenkinsfilePath = jenkinsPipeline.getJenkinsfilePath();
        try {
            byte[] content = getJenkinsPipelineContents(projectPath, jenkinsfilePath).getBytes(UTF_8);
            Files.write(projectPath.resolve(jenkinsfilePath.getFileName()), content, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot copy Jenkinsfile from selected pipeline", e);
        }
    }

    private String getJenkinsPipelineContents(Path projectPath, Path jenkinsFilePath) throws IOException {
        String jenkinsPipelineContent = readJenkinsPipelineFileContent(jenkinsFilePath);
        String snippletName = findSnippetName(jenkinsPipelineContent);
        String snippetsContent = findSnippet(projectPath, snippletName);
        return mergeJenkinsSnippetsIntoPipeline(jenkinsPipelineContent, snippetsContent);
    }

    private String readJenkinsPipelineFileContent(Path jenkinsFilePath) throws IOException {
        return convertBytesToString(Files.readAllBytes(jenkinsFilePath));
    }

    private String findSnippetName(String content) {
        Matcher matcher = INJECT_FRAGMENT.matcher(content);
        return matcher.find() ? matcher.group(1) : "none";
    }

    private String findSnippet(Path projectPath, String name) throws IOException {
        Path snippetsFolder = projectPath.resolve(".openshiftio");
        Path jenkinsSnippet = snippetsFolder.resolve(String.format("Jenkinsfile.%s.snippet", name));

        if (jenkinsSnippet.toFile().exists()) {
            return convertBytesToString(Files.readAllBytes(jenkinsSnippet));
        }
        return "";
    }

    private String mergeJenkinsSnippetsIntoPipeline(String pipelineContent, String snippetsContent) {
        return INJECT_FRAGMENT.matcher(pipelineContent).replaceAll(snippetsContent.replace("$", "\\$"));
    }

    private String convertBytesToString(byte[] bytes) {
        return new String(bytes, UTF_8);
    }
}
