package io.fabric8.launcher.web.endpoints.inputs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;

import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class LaunchProjectileInput implements LauncherProjectileContext {

    public static final String PROJECT_NAME_REGEX = "^[a-zA-Z](?!.*--)(?!.*__)[a-zA-Z0-9-_]{2,38}[a-zA-Z0-9]$";
    public static final int PROJECT_NAME_MAX_LENGTH = 49;
    public static final String PROJECT_NAME_VALIDATION_MESSAGE = "projectName should consist of only alphanumeric characters, '-' and '_'. " +
                                                                    "It should start with alphabetic and end with alphanumeric characters." +
                                                                    "Maximum length of project name is " + PROJECT_NAME_MAX_LENGTH + " characters";

    @FormParam("gitOrganization")
    @Pattern(message = "gitOrganization should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitOrganization;

    @FormParam("gitRepository")
    @NotNull(message = "gitRepository is required")
    @Pattern(message = "gitRepository should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitRepository;

    @FormParam("mission")
    @NotNull(message = "mission is required")
    private Mission mission;

    @FormParam("runtime")
    @NotNull(message = "runtime is required")
    private Runtime runtime;

    @FormParam("runtimeVersion")
    private Version runtimeVersion;

    @FormParam("projectName")
    @NotNull(message = "projectName is required")
    @Pattern(message = PROJECT_NAME_VALIDATION_MESSAGE,
            regexp = PROJECT_NAME_REGEX)
    @Size(message = PROJECT_NAME_VALIDATION_MESSAGE,
            max = PROJECT_NAME_MAX_LENGTH)
    private String projectName;

    @FormParam("groupId")
    @DefaultValue("io.openshift.booster")
    private String groupId;

    @FormParam("artifactId")
    private String artifactId;

    @FormParam("projectVersion")
    @DefaultValue("1.0.0")
    private String projectVersion;

    @HeaderParam("X-Execution-Step-Index")
    @DefaultValue("0")
    private String step;

    @Override
    public Mission getMission() {
        return mission;
    }

    @Override
    public Runtime getRuntime() {
        return runtime;
    }

    @Override
    public Version getRuntimeVersion() {
        return runtimeVersion;
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getProjectVersion() {
        return projectVersion;
    }

    @Override
    public String getGitOrganization() {
        return gitOrganization;
    }

    @Override
    public String getGitRepository() {
        return gitRepository;
    }

    public int getExecutionStep() {
        try {
            return Integer.parseInt(step);
        } catch (Exception e) {
            return 0;
        }
    }

    void setGitOrganization(String gitOrganization) {
        this.gitOrganization = gitOrganization;
    }

    void setGitRepository(String gitRepository) {
        this.gitRepository = gitRepository;
    }

    void setMission(Mission mission) {
        this.mission = mission;
    }

    void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    void setRuntimeVersion(Version runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    void setStep(String step) {
        this.step = step;
    }
}
