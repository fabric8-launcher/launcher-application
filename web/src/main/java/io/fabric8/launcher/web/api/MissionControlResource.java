package io.fabric8.launcher.web.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.core.api.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import static javax.json.Json.createObjectBuilder;

/**
 * Endpoint exposing the {@link MissionControl} over HTTP
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @deprecated superseded by {@link io.fabric8.launcher.web.endpoints.LaunchEndpoint}
 */
@Path(MissionControlResource.PATH_MISSIONCONTROL)
@ApplicationScoped
@Deprecated
public class MissionControlResource {

    /**
     * Paths
     **/
    static final String PATH_MISSIONCONTROL = "/missioncontrol";

    private static final String PATH_UPLOAD = "/upload";

    private static final String PATH_STATUS = "/status";

    private static Logger log = Logger.getLogger(MissionControlResource.class.getName());

    @Resource
    ManagedExecutorService executorService;

    @Inject
    private MissionControl missionControl;

    @Inject
    private Event<StatusMessageEvent> event;

    @POST
    @Path(PATH_UPLOAD)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public void upload(
            @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
            @MultipartForm UploadForm form,
            @Suspended AsyncResponse response) {

        try {
            final java.nio.file.Path tempDir = Files.createTempDirectory("tmpUpload");
            try (InputStream inputStream = form.getFile()) {
                Paths.unzip(inputStream, tempDir);
                try (DirectoryStream<java.nio.file.Path> projects = Files.newDirectoryStream(tempDir)) {
                    java.nio.file.Path project = projects.iterator().next();
                    Projectile projectile = ImmutableLauncherCreateProjectile.builder()
                            .id(UUID.randomUUID())
                            .openShiftProjectName(form.getOpenShiftProjectName())
                            .startOfStep(form.getStartOfStep())
                            .mission(new Mission(form.getMission()))
                            .runtime(new Runtime(form.getRuntime()))
                            .gitRepositoryName(form.getGitHubRepositoryName())
                            .gitRepositoryDescription(form.getGitHubRepositoryDescription())
                            .projectLocation(project)
                            .build();
                    // Fling it
                    // No need to hold off the processing, return the status link immediately
                    response.resume(createObjectBuilder()
                                            .add("uuid", projectile.getId().toString())
                                            .add("uuid_link", PATH_STATUS + "/" + projectile.getId().toString())
                                            .build());
                    try {
                        missionControl.launch(projectile);
                    } catch (Exception ex) {
                        event.fire(new StatusMessageEvent(projectile.getId(), ex));
                        log.log(Level.SEVERE, "Error while launching project", ex);
                    } finally {
                        try {
                            Paths.deleteDirectory(tempDir);
                        } catch (IOException e) {
                            log.log(Level.FINE, "Error while deleting directory " + tempDir, e);
                        }
                    }

                }
            }
        } catch (final IOException e) {
            throw new WebApplicationException("could not unpack zip file into temp folder", e);
        }
    }
}
