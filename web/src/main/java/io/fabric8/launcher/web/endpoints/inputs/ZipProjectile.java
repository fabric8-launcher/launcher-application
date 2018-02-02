package io.fabric8.launcher.web.endpoints.inputs;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.LauncherProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ZipProjectile implements LauncherProjectileContext {

    @FormParam("missionId")
    @NotNull(message = "Mission is required")
    private Mission mission;

    @FormParam("runtimeId")
    @NotNull(message = "Runtime is required")
    private Runtime runtime;

    @FormParam("runtimeVersion")
    private Version runtimeVersion;

    @FormParam("projectName")
    @NotNull(message = "Project Name is required")
    private String projectName;

    @FormParam("groupId")
    private String groupId;

    @FormParam("artifactId")
    private String artifactId;

    @FormParam("projectVersion")
    @DefaultValue("1.0.0")
    private String projectVersion;


    public Mission getMission() {
        return mission;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public Version getRuntimeVersion() {
        return runtimeVersion;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    @Override
    public String getGitRepository() {
        return null;
    }

    @Override
    public String getTargetEnvironment() {
        return null;
    }

    @Override
    public String getPipelineId() {
        return null;
    }

    @Override
    public String getSpacePath() {
        return null;
    }

    @Override
    public String getGitOrganization() {
        return null;
    }
}
