package io.fabric8.launcher.web.endpoints.inputs;

import io.fabric8.launcher.core.api.projectiles.context.CreatorImportLaunchProjectileContext;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.QueryParam;

import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;
import static io.fabric8.launcher.service.openshift.api.OpenShiftService.PROJECT_NAME_REGEX;
import static io.fabric8.launcher.service.openshift.api.OpenShiftService.PROJECT_NAME_VALIDATION_MESSAGE;

public class CreatorImportProjectileInput implements CreatorImportLaunchProjectileContext {

    @QueryParam("applicationName")
    @NotNull(message = "applicationName is required")
    private String applicationName;

    @QueryParam("gitImportUrl")
    @NotNull(message = "gitImportUrl is required")
    private String gitImportUrl;

    @QueryParam("gitImportBranch")
    private String gitImportBranch;

    @QueryParam("gitOrganization")
    @Pattern(message = "gitOrganization should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitOrganization;

    @QueryParam("gitRepository")
    @Pattern(message = "gitRepository should follow consist only of alphanumeric characters, '-', '_' or '.' .",
            regexp = GIT_NAME_REGEXP)
    private String gitRepository;

    @QueryParam("builderImage")
    private String builderImage;

    @QueryParam("builderLanguage")
    private String builderLanguage;

    @QueryParam("projectName")
    @NotNull(message = "projectName is required")
    @Pattern(message = PROJECT_NAME_VALIDATION_MESSAGE,
            regexp = PROJECT_NAME_REGEX)
    private String projectName;

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getGitImportUrl() {
        return gitImportUrl;
    }

    @Override
    public String getGitImportBranch() {
        return gitImportBranch;
    }

    @Override
    public String getGitOrganization() {
        return gitOrganization;
    }

    @Override
    public String getGitRepository() {
        return gitRepository;
    }

    @Override
    public String getBuilderImage() {
        return builderImage;
    }

    @Override
    public String getBuilderLanguage() {
        return builderLanguage;
    }
}
