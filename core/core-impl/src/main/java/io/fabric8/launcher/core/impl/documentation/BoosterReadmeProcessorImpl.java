package io.fabric8.launcher.core.impl.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.documentation.BoosterDocumentationStore;
import io.fabric8.launcher.core.api.documentation.BoosterReadmeProcessor;
import org.apache.commons.lang3.text.StrSubstitutor;

import static io.fabric8.launcher.core.impl.documentation.BoosterReadmePaths.getReadmePropertiesPath;
import static io.fabric8.launcher.core.impl.documentation.BoosterReadmePaths.getReadmeTemplatePath;
import static io.fabric8.launcher.core.impl.documentation.BoosterReadmePaths.loadContents;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newInputStream;

@ApplicationScoped
public class BoosterReadmeProcessorImpl implements BoosterReadmeProcessor {

    private static final Logger logger = Logger.getLogger(BoosterDocumentationStoreImpl.class.getName());

    private final BoosterDocumentationStore boosterDocumentationStore;

    @Inject
    public BoosterReadmeProcessorImpl(final BoosterDocumentationStore boosterDocumentationStore) {
        this.boosterDocumentationStore = boosterDocumentationStore;
    }

    @Override
    public String getReadmeTemplate(final Mission mission) throws IOException {
        final Path path = getReadmeTemplatePath(getDocumentationBasePath(), mission.getId());
        if (!isRegularFile(path)) {
            logger.warning("The requested readme template file does not exist: " + path);
            return null;
        }
        return loadContents(path);
    }

    @Override
    public Map<String, String> getRuntimeProperties(final String deploymentType, final Mission mission, final Runtime runtime) throws IOException {
        Properties props = new Properties();

        final Path path = getReadmePropertiesPath(getDocumentationBasePath(), deploymentType, mission.getId(), runtime.getId());
        if (!isRegularFile(path)) {
            logger.warning("The requested readme property file does not exist: " + path);
            return Collections.emptyMap();
        }

        try (InputStream is = newInputStream(path)) {
            props.load(is);
        }

        @SuppressWarnings("all")
        Map<String, String> map = (Map) props;
        return map;
    }

    @Override
    public String processTemplate(final String template, final Map<String, String> values) {
        StrSubstitutor strSubstitutor = new StrSubstitutor(values);
        strSubstitutor.setEnableSubstitutionInVariables(true);
        return strSubstitutor.replace(template);
    }

    private String getDocumentationBasePath() throws IOException {
        try {
            return boosterDocumentationStore.getDocumentationPath().get().toString();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("An exception occurred while fetching readme.", e);
        } catch (ExecutionException e) {
            throw new IOException("An exception occurred while fetching readme.", e);
        }
    }
}
