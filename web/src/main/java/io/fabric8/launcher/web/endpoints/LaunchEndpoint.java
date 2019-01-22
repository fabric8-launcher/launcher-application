package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.core.api.DefaultMissionControl;
import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.web.endpoints.inputs.LaunchProjectileInput;
import io.fabric8.launcher.web.endpoints.inputs.UploadZipProjectileInput;
import io.fabric8.launcher.web.endpoints.inputs.ZipProjectileInput;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import static java.util.Arrays.asList;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/launcher")
@RequestScoped
public class LaunchEndpoint extends AbstractLaunchEndpoint {

    private static final String APPLICATION_ZIP = "application/zip";

    @Inject
    private DefaultMissionControl missionControl;

    @Inject
    private StatusMessageEventBroker eventBroker;

    @GET
    @Path("/zip")
    @Produces(APPLICATION_ZIP)
    public Response zip(@Valid @BeanParam ZipProjectileInput zipProjectile) throws IOException {
        CreateProjectile projectile = null;
        try {
            projectile = missionControl.prepare(zipProjectile);
            String filename = Objects.toString(zipProjectile.getProjectName(), zipProjectile.getArtifactId());
            byte[] zipContents = Paths.zip(filename, projectile.getProjectLocation());
            return Response
                    .ok(zipContents)
                    .type(APPLICATION_ZIP)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                    .build();
        } finally {
            if (projectile != null) {
                reaper.delete(projectile.getProjectLocation());
            }
        }
    }

    @POST
    @Path("/launch")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public void launch(@Valid @BeanParam LaunchProjectileInput launchProjectileInput, @Suspended AsyncResponse asyncResponse,
                       @Context HttpServletResponse response) throws IOException {
        CreateProjectile projectile = ImmutableLauncherCreateProjectile.builder()
                .from(missionControl.prepare(launchProjectileInput))
                .startOfStep(launchProjectileInput.getExecutionStep())
                .eventConsumer(eventBroker::send)
                .build();
        try {
            doLaunch(projectile, missionControl::launch, asList(LauncherStatusEventKind.values()), response, asyncResponse);
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }

    @POST
    @Path("/upload")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public void uploadZip(@Valid @MultipartForm UploadZipProjectileInput input,
                          @HeaderParam("X-Execution-Step-Index")
                          @DefaultValue("0") int executionStep,
                          @Suspended AsyncResponse asyncResponse,
                          @Context HttpServletResponse response) throws IOException {
        java.nio.file.Path projectDir = Files.createTempDirectory("projectDir");
        Paths.unzip(input.getZipContents(), projectDir);
        java.nio.file.Path projectLocation;
        try (DirectoryStream<java.nio.file.Path> stream =
                     Files.newDirectoryStream(projectDir)) {
            projectLocation = stream.iterator().next();
        }
        CreateProjectile projectile = ImmutableLauncherCreateProjectile.builder()
                .projectLocation(projectLocation)
                .eventConsumer(eventBroker::send)
                .gitOrganization(input.getGitOrganization())
                .gitRepositoryName(input.getGitRepository())
                .startOfStep(executionStep)
                .openShiftProjectName(input.getProjectName())
                .build();
        try {
            doLaunch(projectile, missionControl::launch, asList(LauncherStatusEventKind.values()), response, asyncResponse);
        } finally {
            reaper.delete(projectDir);
        }
    }
}