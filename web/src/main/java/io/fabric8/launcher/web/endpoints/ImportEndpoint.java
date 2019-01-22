package io.fabric8.launcher.web.endpoints;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.projectiles.ImportFromGitProjectile;
import io.fabric8.launcher.core.api.projectiles.context.UploadZipProjectileContext;
import io.fabric8.launcher.web.endpoints.inputs.UploadZipProjectileInput;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import static java.util.Arrays.asList;

@Path("/launcher/import")
@RequestScoped
public class ImportEndpoint extends AbstractLaunchEndpoint {

    @Inject
    private MissionControl<UploadZipProjectileContext, ImportFromGitProjectile> importFromGitMissionControl;

    @POST
    @Path("/git")
    @Produces(MediaType.APPLICATION_JSON)
    public void importFromGit(@Valid @MultipartForm UploadZipProjectileInput input,
                              @HeaderParam("X-Execution-Step-Index")
                              @DefaultValue("0") int executionStep,
                              @Suspended AsyncResponse asyncResponse,
                              @Context HttpServletResponse response) throws IOException {
        ImportFromGitProjectile projectile = importFromGitMissionControl.prepare(input);
        try {
            doLaunch(projectile, importFromGitMissionControl::launch, asList(LauncherStatusEventKind.OPENSHIFT_CREATE,
                                                                             LauncherStatusEventKind.OPENSHIFT_PIPELINE),
                     response, asyncResponse);
        } finally {
            reaper.delete(projectile.getProjectLocation());
        }
    }
}