package io.fabric8.launcher.web.endpoints.inputs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.LauncherProjectileContext;

import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class LaunchProjectileInput implements LauncherProjectileContext {

    @FormParam("gitOrganization")
    @Pattern(message = "gitOrganization should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitOrganization;

    @FormParam("gitRepository")
    @NotNull(message = "gitRepository is required")
    @Pattern(message = "gitRepository should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitRepository;

    @FormParam("missionId")
    @NotNull(message = "missionId is required")
    private Mission mission;

    @FormParam("runtimeId")
    @NotNull(message = "runtimeId is required")
    private Runtime runtime;

    @FormParam("runtimeVersion")
    private Version runtimeVersion;

    @FormParam("projectName")
    @NotNull(message = "projectName is required")
    private String projectName;

    @FormParam("groupId")
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

    public Integer getExecutionStep() {
        try {
            return Integer.parseInt(step);
        } catch (Exception e) {
            return 0;
        }
    }
}
