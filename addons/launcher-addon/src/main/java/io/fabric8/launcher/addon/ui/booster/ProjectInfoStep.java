/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.booster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import io.fabric8.launcher.addon.ui.input.ProjectName;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.readme.ReadmeProcessor;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.jboss.forge.addon.maven.resources.MavenModelResource;
import org.jboss.forge.addon.parser.json.resource.JsonResource;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.furnace.util.Strings;

import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withMission;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withRuntime;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @deprecated
 */
public class ProjectInfoStep implements UIWizardStep {
    private static final Logger logger = Logger.getLogger(ProjectInfoStep.class.getName());

    private static final String RETRY_STEP = "RETRY_STEP";

    @Inject
    private RhoarBoosterCatalog catalog;

    @Inject
    @WithAttributes(label = "Runtime Version")
    private UISelectOne<Version> runtimeVersion;

    @Inject
    private ProjectName named;

    @Inject
    private MissionControlValidator missionControlValidator;

    /**
     * Used in LaunchpadResource
     */
    @Inject
    @WithAttributes(label = "GitHub Repository", note = "If empty, it will assume the project name")
    private UIInput<String> gitHubRepositoryName;

    @Inject
    @WithAttributes(label = "Group Id", defaultValue = "io.openshift.booster", required = true)
    private UIInput<String> groupId;

    @Inject
    @WithAttributes(label = "Artifact Id", required = true)
    private UIInput<String> artifactId;

    @Inject
    @WithAttributes(label = "Version", required = true, defaultValue = "1.0.0-SNAPSHOT")
    private UIInput<String> version;

    @Inject
    private ReadmeProcessor readmeProcessor;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext context = builder.getUIContext();
        Mission mission = (Mission) context.getAttributeMap().get(Mission.class);
        Runtime runtime = (Runtime) context.getAttributeMap().get(Runtime.class);
        artifactId.setDefaultValue(() -> {

            String missionPrefix = (mission == null) ? "" : "-" + mission.getId();
            String runtimeSuffix = (runtime == null) ? "" : "-" + runtime.getId().replaceAll("\\.", "");

            return "booster" + missionPrefix + runtimeSuffix;
        });
        if (mission != null && runtime != null) {
            Set<Version> versions = catalog.getVersions(withMission(mission).and(withRuntime(runtime)));
            if (versions != null && !versions.isEmpty()) {
                runtimeVersion.setValueChoices(versions);
                runtimeVersion.setItemLabelConverter(Version::getName);
                runtimeVersion.setDefaultValue(versions.iterator().next());
                if (showRuntimeVersion()) {
                    builder.add(runtimeVersion);
                }
            }
        }
        DeploymentType deploymentType = (DeploymentType) context.getAttributeMap().get(DeploymentType.class);
        addDeploymentProperties(builder, deploymentType);
        if (isNodeJS(runtime)) {
            // NodeJS only requires the name and version
            artifactId.setLabel("Name");
            version.setDefaultValue("1.0.0");
            if (showArtifactId()) {
                builder.add(artifactId);
            }
            builder.add(version);
        } else {
            builder.add(groupId);
            if (showArtifactId()) {
                builder.add(artifactId);
            }
            builder.add(version);
        }
    }

    @Override
    public void validate(UIValidationContext context) {
        UIContext uiContext = context.getUIContext();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();
        if ("next".equals(attributeMap.get("action"))) {
            // Do not validate again if next() was called
            return;
        }
        List<String> step = (List<String>) attributeMap.get(RETRY_STEP);
        DeploymentType deploymentType = (DeploymentType) attributeMap.get(DeploymentType.class);
        if (deploymentType == DeploymentType.CD && (step == null || "0".equals(step.get(0)))) {
            String openShiftCluster = (String) attributeMap.get("OPENSHIFT_CLUSTER");
            if (missionControlValidator.validateOpenShiftTokenExists(context, openShiftCluster)) {
                missionControlValidator.validateOpenShiftProjectExists(context, named.getValue(), openShiftCluster);
            }
            if (missionControlValidator.validateGitHubTokenExists(context)) {
                String repository = getGithubRepositoryNameValue();
                missionControlValidator.validateGitHubRepositoryExists(context, repository);
            }
        }
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Project Info")
                .description("Project Information")
                .category(Categories.create("Fabric8"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();
        Mission mission = (Mission) attributeMap.get(Mission.class);
        Runtime runtime = (Runtime) attributeMap.get(Runtime.class);
        String openShiftCluster = (String) attributeMap.get("OPENSHIFT_CLUSTER");
        DeploymentType deploymentType = (DeploymentType) attributeMap.get(DeploymentType.class);
        RhoarBooster booster = (RhoarBooster) attributeMap.get(RhoarBooster.class);
        if (booster == null) {
            booster = catalog.getBooster(mission, runtime, runtimeVersion.getValue()).get();
        }
        DirectoryResource initialDir = (DirectoryResource) uiContext.getInitialSelection().get();
        String projectName = named.getValue();
        String artifactIdValue = artifactId.getValue();
        if (Strings.isNullOrEmpty(artifactIdValue)) {
            artifactIdValue = projectName;
        }
        String childDirectory = deploymentType == DeploymentType.CD ? projectName
                : artifactIdValue;
        DirectoryResource projectDirectory = initialDir.getChildDirectory(childDirectory);
        projectDirectory.mkdirs();
        Path projectDirectoryPath = projectDirectory.getUnderlyingResourceObject().toPath();
        // Copy contents
        catalog.copy(booster, projectDirectoryPath);
        // Is it a maven project?
        MavenModelResource modelResource = projectDirectory.getChildOfType(MavenModelResource.class, "pom.xml");

        String runtimeVersionId = null;
        if (runtimeVersion.getValue() != null) {
            runtimeVersionId = runtimeVersion.getValue().getId();
        }

        // Perform model changes
        if (modelResource.exists()) {
            Model model = modelResource.getCurrentModel();
            model.setGroupId(groupId.getValue());
            model.setArtifactId(artifactIdValue);
            model.setVersion(version.getValue());

            String profileId = booster.getMetadata("buildProfile", runtimeVersionId);
            if (profileId != null) {
                // Set the corresponding profile as active
                for (Profile p : model.getProfiles()) {
                    boolean isActive = profileId.equals(p.getId());
                    Activation act = p.getActivation();
                    if (act == null) {
                        act = new Activation();
                        p.setActivation(act);
                    }
                    act.setActiveByDefault(isActive);
                }
            }

            // Change child modules
            for (String module : model.getModules()) {
                DirectoryResource moduleDirResource = projectDirectory.getChildDirectory(module);
                MavenModelResource moduleModelResource = moduleDirResource.getChildOfType(MavenModelResource.class,
                                                                                          "pom.xml");
                Model moduleModel = moduleModelResource.getCurrentModel();
                Parent parent = moduleModel.getParent();
                if (parent != null) {
                    parent.setGroupId(model.getGroupId());
                    parent.setArtifactId(model.getArtifactId());
                    parent.setVersion(model.getVersion());
                    moduleModelResource.setCurrentModel(moduleModel);
                }
            }
            modelResource.setCurrentModel(model);
        }

        // If NodeJS, just change name and version
        if (isNodeJS(runtime)) {
            JsonResource packageJsonResource = projectDirectory.getChildOfType(JsonResource.class, "package.json");
            if (packageJsonResource.exists()) {
                JsonObjectBuilder job = Json.createObjectBuilder();
                job.add("name", artifactIdValue);
                job.add("version", version.getValue());
                for (Entry<String, JsonValue> entry : packageJsonResource.getJsonObject().entrySet()) {
                    String key = entry.getKey();
                    // Do not copy name or version
                    if (key.equals("name") || key.equals("version")) {
                        continue;
                    }
                    job.add(key, entry.getValue());
                }
                packageJsonResource.setContents(job.build());
            }
        }
        // Create README.adoc file
        try {
            String template = getReadmeTemplate(mission);
            if (template != null) {
                Map<String, String> values = new HashMap<>();
                values.put("missionId", mission.getId());
                values.put("mission", mission.getName());
                values.put("runtimeId", runtime.getId());
                values.put("runtime", runtime.getName());
                if (runtimeVersion.getValue() != null) {
                    values.put("runtimeVersion", runtimeVersion.getValue().getKey());
                } else {
                    values.put("runtimeVersion", "");
                }
                values.put("openShiftProject", projectName);
                values.put("openShiftCluster", openShiftCluster);
                values.put("groupId", groupId.getValue());
                values.put("artifactId", artifactIdValue);
                values.put("version", version.getValue());
                values.put("targetRepository", Objects.toString(gitHubRepositoryName.getValue(), projectName));
                values.putAll(getRuntimeProperties(deploymentType, mission, runtime));
                String readmeOutput = getReadmeProcessor().processTemplate(template, values);
                projectDirectory.getChildOfType(FileResource.class, "README.adoc").setContents(readmeOutput);
                // Delete README.md
                projectDirectory.getChildOfType(FileResource.class, "README.md").delete();
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                logger.log(Level.WARNING, "No README.adoc and properties found for " + mission.getId() + " " + runtime.getId() +
                        ". Check to see if there is a corresponding properties file for your Mission, Runtime, and DeploymentType here: " +
                        "https://github.com/fabric8-launcher/launcher-documentation/tree/master/docs/topics/readme");

            } else {
                logger.log(Level.SEVERE, "Error while creating README.adoc", e);
            }
        }

        uiContext.setSelection(projectDirectory);
        Map<String, String> returnValue = new HashMap<>();
        returnValue.put("mission", mission.getId());
        returnValue.put("runtime", runtime.getId());
        if (runtimeVersionId != null) {
            returnValue.put("runtimeVersion", runtimeVersionId);
        }
        returnValue.put("named", projectName);
        returnValue.put("gitHubRepositoryName", Objects.toString(gitHubRepositoryName.getValue(), projectName));
        returnValue.put("gitHubRepositoryDescription",
                        "Generated by the Red Hat Developer Launch (https://developers.redhat.com/launch)");
        returnValue.put("openShiftProjectName", projectName);
        returnValue.put("openShiftCluster", openShiftCluster);
        returnValue.put("artifactId", artifactIdValue);
        // Add Steps
        List<String> steps = (List<String>) attributeMap.get(RETRY_STEP);
        if (steps != null) {
            returnValue.put("step", steps.get(0));
        }

        return Results.success("", returnValue);
    }

    protected void addDeploymentProperties(UIBuilder builder, DeploymentType deploymentType) {
        if (deploymentType == DeploymentType.CD) {
            builder.add(named).add(gitHubRepositoryName);
        }
    }

    protected Map<String, String> getRuntimeProperties(DeploymentType deploymentType, Mission mission, Runtime runtime) throws IOException {
        return getReadmeProcessor().getRuntimeProperties(deploymentType.name().toLowerCase(), mission, runtime);
    }

    protected String getReadmeTemplate(Mission mission) throws IOException {
        return getReadmeProcessor().getReadmeTemplate(mission);
    }

    /**
     * Strategy method allowing derived classes to use custom logic to decide if we should show or hide the artifactID
     *
     * @return true if we should show it
     */
    protected boolean showArtifactId() {
        return true;
    }

    protected boolean showRuntimeVersion() {
        return true;
    }

    protected ReadmeProcessor getReadmeProcessor() {
        return readmeProcessor;
    }

    protected ProjectName getNamed() {
        return named;
    }

    protected UIInput<String> getGitHubRepositoryName() {
        return gitHubRepositoryName;
    }

    protected UIInput<String> getGroupId() {
        return groupId;
    }

    protected UIInput<String> getArtifactId() {
        return artifactId;
    }

    protected UIInput<String> getVersion() {
        return version;
    }

    protected String getGithubRepositoryNameValue() {
        String repository = gitHubRepositoryName.getValue();
        if (Strings.isNullOrEmpty(repository)) {
            repository = named.getValue();
        }
        return repository;
    }

    private boolean isNodeJS(Runtime runtime) {
        return runtime != null && "nodejs".equals(runtime.getId());
    }
}
