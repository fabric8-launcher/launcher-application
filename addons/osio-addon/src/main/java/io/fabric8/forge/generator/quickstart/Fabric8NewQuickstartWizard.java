/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.generator.quickstart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.fabric8.forge.addon.utils.archetype.FabricArchetypeCatalogFactory;
import io.fabric8.forge.generator.CommonSteps;
import io.fabric8.launcher.addon.ui.input.ProjectName;
import io.fabric8.launcher.addon.ui.input.TopLevelPackage;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.maven.archetype.ArchetypeHelper;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.jboss.forge.furnace.services.Imported;

import static io.openshift.booster.Files.deleteRecursively;


/**
 */
public class Fabric8NewQuickstartWizard implements UIWizard {
    /**
     * Files to be deleted after project creation (if exists)
     */
    private static final String[] FILES_TO_BE_DELETED = {".git", ".travis", ".travis.yml"};

    protected static final String ARCHETYPE_SUFFIX = "-archetype";

    @Inject
    @WithAttributes(label = "Project type", required = true)
    private UISelectOne<QuickstartDTO> type;

    @Inject
    private ProjectName named;

    @Inject
    private TopLevelPackage topLevelPackage;

    @Inject
    @WithAttributes(label = "Project version", required = true, defaultValue = "1.0.0-SNAPSHOT")
    private UIInput<String> version;

    @Inject
    private ResourceFactory resourceFactory;

    @Inject
    private FabricArchetypeCatalogFactory archetypeCatalogFactory;

    @Inject
    private Imported<DependencyResolver> resolver;

    public static String getLabel(Archetype archetype) {
        String answer = archetype.getArtifactId();
        if (answer.endsWith(ARCHETYPE_SUFFIX)) {
            answer = answer.substring(0, answer.length() - ARCHETYPE_SUFFIX.length());
        }
        return answer.replace('-', ' ');
    }

    public static String toId(Archetype archetype) {
        return archetype.getGroupId() + ":" + archetype.getArtifactId() + ":" + archetype.getVersion();
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext uiContext = builder.getUIContext();

        if (uiContext.getProvider().isGUI()) {
            type.setItemLabelConverter(QuickstartDTO::getName);
        } else {
            type.setItemLabelConverter(QuickstartDTO::getId);
        }
        List<QuickstartDTO> quickstarts = getQuickstarts();
        type.setValueChoices(quickstarts);
        if (!quickstarts.isEmpty()) {
            type.setDefaultValue(findDefaultQuickstart(quickstarts));
        }
        Callable<String> description = () -> type.getValue() != null ? type.getValue().getDescription() : null;
        type.setDescription(description).setNote(description);
        builder.add(type).add(named).add(topLevelPackage).add(version);
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Fabric8: New Quickstart")
                .description("Generate your project from a quickstart")
                .category(Categories.create("Fabric8"));
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
        attributeMap.put("name", named.getValue());
        attributeMap.put("type", type.getValue());

        NavigationResultBuilder builder = NavigationResultBuilder.create();
        CommonSteps.addPipelineGitHubAndOpenShiftSteps(builder);
        return builder.build();

    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        QuickstartDTO qs = type.getValue();
        System.out.println("About to create quickstart: " + qs);

        String id = qs.getId();
        Archetype archetype = getArchetypeForId(id);
        if (archetype == null) {
            return Results.fail("Could not find the archetype for: " + id);
        }

        File projectDir = Files.createTempDirectory("projectdir").toFile();

        Coordinate coordinate = CoordinateBuilder.create()
                .setGroupId(archetype.getGroupId())
                .setArtifactId(archetype.getArtifactId())
                .setVersion(archetype.getVersion())
                .setPackaging("jar");

        Dependency dependency = resolver.get().resolveArtifact(DependencyQueryBuilder.create(coordinate));
        if (dependency == null) {
            return Results.fail("Could not find archetype " + archetype);
        }
        FileResource<?> artifact = dependency.getArtifact();
        if (artifact == null || !artifact.exists() || !artifact.isReadable()) {
            return Results.fail("Archetype " + archetype + " has no associated artifact");

        }
        InputStream archetypeIn = artifact.getResourceInputStream();
        if (archetypeIn == null) {
            return Results.fail("Could not read archetype " + archetype + " resource stream");
        }

        String groupId = topLevelPackage.getValue();
        String artifactId = this.named.getValue();
        String version = this.version.getValue();
        ArchetypeHelper generator = new ArchetypeHelper(archetypeIn, projectDir, groupId, artifactId, version);
        int result = generator.execute();
        System.out.println("Result of generating " + archetype + " to " + projectDir + " was: " + result);

        // Delete unwanted files
        deleteUnwantedFiles(projectDir);
        context.getUIContext().setSelection(projectDir);
        return Results.success();
    }

    protected QuickstartDTO findDefaultQuickstart(List<QuickstartDTO> quickstarts) {
        for (QuickstartDTO quickstart : quickstarts) {
            if (quickstart.getId().startsWith("vertx")) {
                return quickstart;
            }
        }
        return quickstarts.get(0);
    }

    private Archetype getArchetypeForId(String id) {
        List<Archetype> archetypes = getArchetypes();
        for (Archetype archetype : archetypes) {
            if (id.equals(toId(archetype))) {
                return archetype;
            }
        }
        return null;
    }

    private List<QuickstartDTO> getQuickstarts() {
        List<QuickstartDTO> answer = new ArrayList<>();
        List<Archetype> archetypes = getArchetypes();
        for (Archetype archetype : archetypes) {
            answer.add(createQuickstart(archetype));
        }
        Collections.sort(answer);
        return answer;
    }

    protected List<Archetype> getArchetypes() {
        ArchetypeCatalog archetypeCatalog = archetypeCatalogFactory.getArchetypeCatalog();
        return archetypeCatalog.getArchetypes();
    }

    private QuickstartDTO createQuickstart(Archetype archetype) {
        QuickstartDTO answer = new QuickstartDTO();
        answer.setId(toId(archetype));
        answer.setName(getLabel(archetype));
        answer.setDescription(archetype.getDescription());
        return answer;
    }

    private void deleteUnwantedFiles(File projectDir) {
        for (String file : FILES_TO_BE_DELETED) {
            Path pathToDelete = projectDir.toPath().resolve(file);
            try {
                deleteRecursively(pathToDelete);
            } catch (IOException ignored) {
            }
        }
    }
}
