package io.fabric8.launcher.osio.preparers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
    private static final String INJECT_FRAGMENT_PART = "// INJECT FRAGMENT ";
    private static final Pattern INJECT_FRAGMENT = Pattern.compile(INJECT_FRAGMENT_PART + "(.*)");

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
        List<String> snippetNames = findSnippetNames(jenkinsPipelineContent);

        for (String snippetName : snippetNames) {
            String snippetsContent = findSnippet(projectPath, snippetName);
            jenkinsPipelineContent = mergeJenkinsSnippetIntoPipeline(jenkinsPipelineContent, snippetName, snippetsContent);
        }
        return jenkinsPipelineContent;
    }

    private String readJenkinsPipelineFileContent(Path jenkinsFilePath) throws IOException {
        return convertBytesToString(Files.readAllBytes(jenkinsFilePath));
    }

    private List<String> findSnippetNames(String content) {
        List<String> result = new ArrayList<>();
        Matcher matcher = INJECT_FRAGMENT.matcher(content);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }

        return result;
    }

    private String findSnippet(Path projectPath, String name) throws IOException {
        Path snippetsFolder = projectPath.resolve(".openshiftio");
        Path jenkinsSnippet = snippetsFolder.resolve(String.format("Jenkinsfile.%s.snippet", name));

        if (jenkinsSnippet.toFile().exists()) {
            return convertBytesToString(Files.readAllBytes(jenkinsSnippet));
        }
        return "";
    }

    private String mergeJenkinsSnippetIntoPipeline(String pipelineContent, String snippetName, String snippetsContent) {
        return Pattern.compile(INJECT_FRAGMENT_PART + snippetName).matcher(pipelineContent).replaceAll(
                snippetsContent.replace("$", "\\$"));
    }

    private String convertBytesToString(byte[] bytes) {
        return new String(bytes, UTF_8);
    }
}
