package io.fabric8.launcher.web.api;

import java.io.InputStream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.core.api.ProjectileContext;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @deprecated superseded by {@link ProjectileContext}
 */
@Deprecated
public class UploadForm {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private InputStream file;

    @FormParam("openShiftProjectName")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String openShiftProjectName;

    @FormParam("gitHubRepositoryName")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String gitHubRepositoryName;

    @FormParam("gitHubRepositoryDescription")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String gitHubRepositoryDescription;

    @FormParam("mission")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String mission;

    @FormParam("runtime")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String runtime;

    @FormParam("openShiftCluster")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String openShiftCluster;

    @FormParam("step")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String step;

    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }

    public String getGitHubRepositoryName() {
        return gitHubRepositoryName;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    public String getMission() {
        return mission;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getOpenShiftProjectName() {
        return openShiftProjectName;
    }

    public String getOpenShiftCluster() {
        return openShiftCluster;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public int getStartOfStep() {
        try {
            return Integer.parseInt(step);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
