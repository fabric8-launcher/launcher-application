package io.fabric8.launcher.osio.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.yaml.snakeyaml.Yaml;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class JenkinsPipelineRegistry {
    private Set<JenkinsPipeline> pipelines = Collections.emptySet();

    private static final Logger log = Logger.getLogger(JenkinsPipelineRegistry.class.getName());

    /**
     * Builds the registry index. Should be called only once
     */
    @PostConstruct
    public void index() {
        try {
            Path pipelinesPath = resolvePipelinesPath();

            Set<JenkinsPipeline> pipes = new TreeSet<>(Comparator.comparing(JenkinsPipeline::getId));
            Files.walkFileTree(pipelinesPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = file.getFileName().toString();
                    if (name.equals("metadata.yml") || name.equals("metadata.yaml")) {
                        JenkinsPipeline p = readPipeline(file);
                        if (p != null) {
                            pipes.add(p);
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });
            pipelines = Collections.unmodifiableSet(pipes);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to index Jenkins pipelines", e);
        }
    }

    private Path resolvePipelinesPath() throws IOException {
        Path targetPath = Files.createTempDirectory("pipeline-library");
        try (InputStream is = getClass().getResourceAsStream("/jenkinsfiles.zip")) {
            io.fabric8.launcher.base.Paths.unzip(is, targetPath);
        }
        return targetPath;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private JenkinsPipeline readPipeline(Path metadataFile) {
        Yaml yaml = new Yaml();
        try (BufferedReader reader = Files.newBufferedReader(metadataFile)) {
            Map<String, Object> metadata = yaml.loadAs(reader, Map.class);
            String platform = metadataFile.getParent().getParent().getFileName().toString();
            String name = metadataFile.getParent().getFileName().toString();
            String id = (platform + "-" + name).toLowerCase();
            String description = readDescription(metadataFile.getParent());
            Path jenkinsfilePath = metadataFile.getParent().resolve("Jenkinsfile");
            boolean suggested = Boolean.valueOf(Objects.toString(metadata.getOrDefault("suggested", "false")));
            boolean techPreview = Boolean.valueOf(Objects.toString(metadata.getOrDefault("tech-preview", "false")));

            List<JenkinsPipeline.Stage> stages = (metadata.get("stages") instanceof List ? (List<Map<String, String>>) metadata.get("stages") : Collections.<Map<String, String>>emptyList())
                    .stream().map(s -> ImmutableStage.of(s.getOrDefault("name", "<no name>"), s.getOrDefault("description", "<no description>")))
                    .collect(Collectors.toList());
            ImmutableJenkinsPipeline.Builder builder = ImmutableJenkinsPipeline.builder()
                    .id(id)
                    .platform(platform)
                    .name(humanize(name))
                    .description(description)
                    .isSuggested(suggested)
                    .isTechPreview(techPreview)
                    .stages(stages)
                    .jenkinsfilePath(jenkinsfilePath);
            return builder.build();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while reading " + metadataFile, e);
            return null;
        }
    }

    private String readDescription(Path folder) {
        Path mdPath = folder.resolve("ReadMe.md");
        if (Files.isRegularFile(mdPath)) {
            try {
                return new String(Files.readAllBytes(mdPath)).trim();
            } catch (IOException e) {
                log.log(Level.WARNING, "Couldn't read pipeline description: " + mdPath, e);
            }
        }
        return "";
    }

    public Collection<JenkinsPipeline> getFilteredPipelines(Predicate<JenkinsPipeline> predicate) {
        return pipelines.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public Optional<JenkinsPipeline> getFilteredPipeline(Predicate<JenkinsPipeline> predicate) {
        return pipelines.stream()
                .filter(predicate)
                .findAny();
    }

    public Collection<JenkinsPipeline> getPipelines(@Nullable String platform) {
        if (platform != null) {
            return getFilteredPipelines(p -> platform.equalsIgnoreCase(p.getPlatform()));
        } else {
            return pipelines;
        }
    }

    public Optional<JenkinsPipeline> findPipelineById(String pipelineId) {
        return getFilteredPipeline(p -> pipelineId.equals(p.getId()));
    }

    private String humanize(String label) {
        return splitCamelCase(label, ", ")
                .replace(", And, ", " and ");
    }

    private static String splitCamelCase(String text, String separator) {
        StringBuilder buffer = new StringBuilder();
        char last = 'A';
        for (char c: text.toCharArray()) {
            if (Character.isLowerCase(last) && Character.isUpperCase(c)) {
                buffer.append(separator);
            }
            buffer.append(c);
            last = c;
        }
        return buffer.toString();
    }

}
