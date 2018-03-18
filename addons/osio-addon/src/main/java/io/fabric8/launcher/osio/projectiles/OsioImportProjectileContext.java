package io.fabric8.launcher.osio.projectiles;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;
import javax.validation.constraints.Size;

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
    @Size(message = "Space Path must be in the range of 4 to 63 characters long", min = 5, max = 64)
    @Pattern(message = "Space Path should start with a slash (/)."
            + " must contain only letters, numbers, spaces, underscores (_) or hyphens (-)"
            + " It cannot start or end with a space, an underscore or a hyphen.",
            regexp = "\\/[a-zA-Z\\d][a-zA-Z\\d\\s_-]*[a-zA-Z\\d]$")
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
