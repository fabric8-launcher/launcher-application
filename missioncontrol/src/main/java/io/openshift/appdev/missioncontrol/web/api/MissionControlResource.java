package io.openshift.appdev.missioncontrol.web.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.ForkProjectile;
import io.openshift.appdev.missioncontrol.core.api.MissionControl;
import io.openshift.appdev.missioncontrol.core.api.ProjectileBuilder;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.service.keycloak.api.KeycloakService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

/**
 * Endpoint exposing the {@link MissionControl} over HTTP
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(MissionControlResource.PATH_MISSIONCONTROL)
@ApplicationScoped
public class MissionControlResource extends AbstractResource {

    /**
     * Paths
     **/
    static final String PATH_MISSIONCONTROL = "/missioncontrol";

    private static final String PATH_LAUNCH = "/launch";

    private static final String PATH_UPLOAD = "/upload";

    private static final String PATH_STATUS = "/status";

    /*
     MissionControl Query Parameters
     */
    private static final String QUERY_PARAM_SOURCE_REPO = "sourceRepo";

    private static final String QUERY_PARAM_GIT_REF = "gitRef";

    private static final String QUERY_PARAM_PIPELINE_TEMPLATE_PATH = "pipelineTemplatePath";

    private static Logger log = Logger.getLogger(MissionControlResource.class.getName());

    @Inject
    private MissionControl missionControl;

    @Inject
    private Event<StatusMessageEvent> event;

    @Resource
    ManagedExecutorService executorService;

    @GET
    @Path(PATH_LAUNCH)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JsonObject fling(
            @Context final HttpServletRequest request,
            @NotNull @QueryParam(QUERY_PARAM_SOURCE_REPO) final String sourceGitHubRepo,
            @NotNull @QueryParam(QUERY_PARAM_GIT_REF) final String gitRef,
            @NotNull @QueryParam(QUERY_PARAM_PIPELINE_TEMPLATE_PATH) final String pipelineTemplatePath,
            @NotNull @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        Identity githubIdentity;
        Identity openShiftIdentity;
        if (useDefaultIdentities()) {
            githubIdentity = getDefaultGithubIdentity();
            openShiftIdentity = getDefaultOpenShiftIdentity();
        } else {
            KeycloakService keycloakService = this.keycloakServiceInstance.get();
            githubIdentity = keycloakService.getGitHubIdentity(authorization);
            openShiftIdentity = keycloakService.getOpenShiftIdentity(authorization);
        }

        ForkProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(githubIdentity)
                .openShiftIdentity(openShiftIdentity)
                .forkType()
                .sourceGitHubRepo(sourceGitHubRepo)
                .gitRef(gitRef)
                .pipelineTemplatePath(pipelineTemplatePath)
                .build();
        // Fling it
        executorService.submit(() -> missionControl.launch(projectile));
        return Json.createObjectBuilder()
                .add("uuid", projectile.getId().toString())
                .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                .build();
    }

    @POST
    @Path(PATH_UPLOAD)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject upload(
            @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
            @MultipartForm UploadForm form) {

        Identity githubIdentity = getGitHubIdentity(authorization);
        Identity openShiftIdentity = getOpenShiftIdentity(authorization, form.getOpenShiftCluster());
        try {
            final java.nio.file.Path tempDir = Files.createTempDirectory("tmpUpload");
            try (InputStream inputStream = form.getFile()) {
                FileUploadHelper.unzip(inputStream, tempDir);
                try (DirectoryStream<java.nio.file.Path> projects = Files.newDirectoryStream(tempDir)) {
                    java.nio.file.Path project = projects.iterator().next();
                    CreateProjectile projectile = ProjectileBuilder.newInstance()
                            .gitHubIdentity(githubIdentity)
                            .openShiftIdentity(openShiftIdentity)
                            .openShiftProjectName(form.getOpenShiftProjectName())
                            .openShiftClusterName(form.getOpenShiftCluster())
                            .createType()
                            .mission(form.getMission())
                            .runtime(form.getRuntime())
                            .gitHubRepositoryName(form.getGitHubRepositoryName())
                            .gitHubRepositoryDescription(form.getGitHubRepositoryDescription())
                            .projectLocation(project)
                            .build();
                    // Fling it
                    CompletableFuture.supplyAsync(() -> missionControl.launch(projectile), executorService)
                            .whenComplete((boom, ex) -> {
                                if (ex != null) {
                                    event.fire(new StatusMessageEvent(projectile.getId(), ex));
                                    log.log(Level.SEVERE, "Error while launching project", ex);
                                }

                                FileUploadHelper.deleteDirectory(tempDir);
                            });
                    return Json.createObjectBuilder()
                            .add("uuid", projectile.getId().toString())
                            .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                            .build();
                }
            }
        } catch (final IOException e) {
            throw new WebApplicationException("could not unpack zip file into temp folder", e);
        }
    }
}
