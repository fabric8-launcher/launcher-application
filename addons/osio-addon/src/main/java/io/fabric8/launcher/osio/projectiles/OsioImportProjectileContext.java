package io.fabric8.launcher.osio.projectiles;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;

public class OsioImportProjectileContext {

    @FormParam("gitOrganization")
    private String gitOrganization;

    @FormParam("gitRepository")
    @NotNull(message = "Git repository is required")
    private String gitRepository;

    @FormParam("projectName")
    @NotNull(message = "Project Name is required")
    private String projectName;

    @FormParam("pipelineId")
    @NotNull
    private String pipelineId;

    @FormParam("spacePath")
    @NotNull
    @Pattern(message = "Space Path should start with a /", regexp = "\\/[a-zA-Z]*")
    private String spacePath;

    public String getGitOrganization() {
        return gitOrganization;
    }

    public String getGitRepository() {
        return gitRepository;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public String getSpacePath() {
        return spacePath;
    }
}
