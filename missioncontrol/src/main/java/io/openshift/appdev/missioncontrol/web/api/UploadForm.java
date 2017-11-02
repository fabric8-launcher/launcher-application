package io.openshift.appdev.missioncontrol.web.api;

import java.io.InputStream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
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

    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }

    public void setGitHubRepositoryDescription(String gitHubRepositoryDescription) {
        this.gitHubRepositoryDescription = gitHubRepositoryDescription;
    }

    public String getGitHubRepositoryName() {
        return gitHubRepositoryName;
    }

    public void setGitHubRepositoryName(String gitHubRepositoryName) {
        this.gitHubRepositoryName = gitHubRepositoryName;
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

    public void setMission(String mission) {
        this.mission = mission;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getOpenShiftProjectName() {
        return openShiftProjectName;
    }

    public void setOpenShiftProjectName(String openShiftProjectName) {
        this.openShiftProjectName = openShiftProjectName;
    }

    public String getOpenShiftCluster() {
        return openShiftCluster;
    }

    public void setOpenShiftCluster(String openShiftCluster) {
        this.openShiftCluster = openShiftCluster;
    }
}