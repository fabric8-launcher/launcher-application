package io.fabric8.launcher.osio.projectiles;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;

import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;

public class OsioImportProjectileContext {

    @FormParam("gitOrganization")
    @Pattern(message = "gitOrganization should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitOrganization;

    @FormParam("gitRepository")
    @NotNull(message = "gitRepository is required")
    @Pattern(message = "gitRepository should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitRepository;

    @FormParam("projectName")
    @NotNull(message = "projectName is required")
    @Pattern(message = "projectName should follow the same pattern as a DNS-1123 subdomain " +
            "and must consist of lower case alphanumeric characters, '-' or '.', and must start " +
            "and end with an alphanumeric character",
            regexp = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*")
    private String projectName;

    @FormParam("pipelineId")
    @NotNull(message = "pipelineId is required")
    private String pipelineId;

    @FormParam("spaceId")
    @NotNull(message = "spaceId is required")
    private String spaceId;

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

    public String getSpaceId() {
        return spaceId;
    }
}
