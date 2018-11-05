package io.fabric8.launcher.web.endpoints.inputs;

import java.io.InputStream;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.core.api.projectiles.context.UploadZipProjectileContext;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;
import static io.fabric8.launcher.service.openshift.api.OpenShiftService.PROJECT_NAME_REGEX;
import static io.fabric8.launcher.service.openshift.api.OpenShiftService.PROJECT_NAME_VALIDATION_MESSAGE;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class UploadZipProjectileInput implements UploadZipProjectileContext {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private InputStream zipContents;

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
    @Pattern(message = PROJECT_NAME_VALIDATION_MESSAGE,
            regexp = PROJECT_NAME_REGEX)
    private String projectName;

    @Override
    public InputStream getZipContents() {
        return zipContents;
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
    public String getProjectName() {
        return projectName;
    }
}
