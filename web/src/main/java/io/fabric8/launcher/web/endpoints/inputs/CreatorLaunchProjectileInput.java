package io.fabric8.launcher.web.endpoints.inputs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.core.api.projectiles.context.CreatorLauncherProjectileContext;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;

import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;
import static io.fabric8.launcher.service.openshift.api.OpenShiftService.PROJECT_NAME_REGEX;
import static io.fabric8.launcher.service.openshift.api.OpenShiftService.PROJECT_NAME_VALIDATION_MESSAGE;

public class CreatorLaunchProjectileInput implements CreatorLauncherProjectileContext {

    @FormParam("project")
    private ObjectNode project;

    @FormParam("projectName")
    @NotNull(message = "projectName is required")
    @Pattern(message = PROJECT_NAME_VALIDATION_MESSAGE,
            regexp = PROJECT_NAME_REGEX)
    private String projectName;

    @FormParam("gitOrganization")
    @Pattern(message = "gitOrganization should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitOrganization;

    @FormParam("gitRepository")
    @Pattern(message = "gitRepository should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitRepository;

    @Override
    public ObjectNode getProject() {
        return project;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getGitOrganization() {
        return gitOrganization;
    }

    @Override
    public String getGitRepository() {
        return gitRepository;
    }
}
