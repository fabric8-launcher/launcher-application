package io.fabric8.launcher.web.endpoints.inputs;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;

import io.fabric8.launcher.core.api.LauncherProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class LaunchProjectile extends ZipProjectile implements LauncherProjectileContext {

    @FormParam("targetEnvironment")
    private String targetEnvironment;

    @FormParam("pipelineId")
    private String pipelineId;

    @FormParam("spacePath")
    private String spacePath;

    @FormParam("gitOrganization")
    private String gitOrganization;

    @FormParam("gitRepository")
    @NotNull(message = "Git repository is required")
    private String gitRepository;

    @Override
    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    @Override
    public String getPipelineId() {
        return pipelineId;
    }

    @Override
    public String getSpacePath() {
        return spacePath;
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
