package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.core.api.DefaultMissionControl;
import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.events.StatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.projectiles.context.CreatorLaunchProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.CreatorLaunchingProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.CreatorZipProjectileContext;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo;
import io.fabric8.launcher.creator.catalog.GeneratorInfo;
import io.fabric8.launcher.creator.core.analysis.AnalyzeKt;
import io.fabric8.launcher.creator.core.analysis.GitKt;
import io.fabric8.launcher.creator.core.catalog.EnumsKt;
import io.fabric8.launcher.creator.core.deploy.ApplyKt;
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor;
import io.fabric8.launcher.creator.core.resource.BuilderImage;
import io.fabric8.launcher.creator.core.resource.ImagesKt;
import io.fabric8.launcher.web.endpoints.inputs.CreatorImportProjectileInput;
import io.fabric8.launcher.web.producers.CacheProducer.AppPath;
import org.apache.commons.lang3.StringUtils;
import org.cache2k.Cache;
import org.jboss.logmanager.Level;

import static io.fabric8.launcher.base.JsonUtils.createArrayNode;
import static io.fabric8.launcher.base.JsonUtils.createObjectNode;
import static io.fabric8.launcher.base.JsonUtils.toArrayNode;
import static io.fabric8.launcher.base.JsonUtils.toObjectNode;
import static java.util.Arrays.asList;

@Path("/creator")
@RequestScoped
public class CreatorEndpoint extends AbstractLaunchEndpoint {

    private static final Logger logger = Logger.getLogger(CreatorEndpoint.class.getName());

    @Inject
    DefaultMissionControl missionControl;

    @Inject
    StatusMessageEventBroker eventBroker;

    @Inject
    Instance<ProjectilePreparer> preparers;

    @Inject
    Cache<String, AppPath> pathCache;

    @GET
    @Path("/capabilities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilities() {
        return Response.ok(CapabilityInfo.Companion.getInfos()).build();
    }

    @GET
    @Path("/generators")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGenerators() {
        return Response.ok(GeneratorInfo.Companion.getInfoDefs()).build();
    }

    @GET
    @Path("/enums")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnums() {
        // TODO filtering
        return Response.ok(EnumsKt.listEnums()).build();
    }

    @GET
    @Path("/enums/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnums(@NotNull(message = "enumeration 'id' is required") @PathParam("id") String id) {
        // TODO filtering
        if (EnumsKt.listEnums().containsKey(id)) {
            return Response.ok(EnumsKt.enumById(id)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response zip(@Valid CreatorZipProjectileContext input) {
        DeploymentDescriptor deployment = toDescriptor(input.getProject());
        return ApplyKt.withDeployment(deployment, projectLocation -> {
            String appName = deployment.getApplications().get(0).getApplication();
            try {
                java.nio.file.Path tmp = Files.createTempFile("creator-", ".zip");
                try (OutputStream out = Files.newOutputStream(tmp)) {
                    Paths.zip(appName, projectLocation, out);
                    String key = UUID.randomUUID().toString();
                    pathCache.put(key, new AppPath(appName, tmp));
                    return Response.ok(createObjectNode().put("id", key)).build();
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    public Response getDownload(@NotNull(message = "download 'id' is required") @QueryParam("id") String id) throws IOException {
        AppPath ap = pathCache.get(id);
        if (ap == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        byte[] zipContents = Files.readAllBytes(ap.path);
        return Response
                .ok(zipContents)
                .type("application/zip")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ap.name + ".zip\"")
                .build();
    }

    @POST
    @Path("/launch")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response launch(@Valid CreatorLaunchProjectileContext input,
                           @HeaderParam("X-Execution-Step-Index")
                           @DefaultValue("0") int executionStep,
                           @Suspended AsyncResponse asyncResponse,
                           @Context HttpServletResponse response) {
        DeploymentDescriptor deployment = toDescriptor(input.getProject());
        return performLaunch(deployment, input, executionStep, asyncResponse, response);
    }

    @GET
    @Path("/import/branches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGitBranches(@NotNull(message = "'gitImportUrl' is required") @QueryParam("gitImportUrl") String gitImportUrl) {
        if (!gitImportUrl.startsWith("http:") && !gitImportUrl.startsWith("https:") && !gitImportUrl.startsWith("git@")) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            return Response.ok(GitKt.listBranchesFromGit(gitImportUrl)).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/import/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnalysis(@NotNull(message = "'gitImportUrl' is required") @QueryParam("gitImportUrl") String gitImportUrl,
                                @QueryParam("gitImportBranch") String gitImportBranch) {
        if (!gitImportUrl.startsWith("http:") && !gitImportUrl.startsWith("https:") && !gitImportUrl.startsWith("git@")) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            final ObjectNode response = createObjectNode();

            ArrayNode importables = GitKt.withGitRepo(gitImportUrl, gitImportBranch, root -> {
                Map<String, Object> tree = AnalyzeKt.folderTree(root);
                response.set("folders", toObjectNode(tree));

                List<java.nio.file.Path> folders = AnalyzeKt.listFolders(root);
                return createArrayNode().addAll(folders.stream().flatMap(folder -> {
                    BuilderImage img = AnalyzeKt.determineBuilderImage(root.resolve(folder));
                    if (img != null) {
                        final ObjectNode importable = createObjectNode();
                        importable.put("folder", folder.toString());
                        importable.put("image", img.getId());
                        return Stream.of(importable);
                    } else {
                        return Stream.empty();
                    }
                }).collect(Collectors.toList()));
            });
            response.set("importables", importables);

            // TODO deprecated, remove once the frontend uses the new response layout
            if (importables.size() > 0) {
                response.set("image", importables.get(0).get("image"));
            }

            List<BuilderImage> imgs = ImagesKt.getBuilderImages();
            response.set("builderImages", toArrayNode(imgs));

            return Response.ok(response).build();
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Error while analyzing sources from " + gitImportUrl, ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/import/launch")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response launch(@Valid @BeanParam CreatorImportProjectileInput input,
                           @HeaderParam("X-Execution-Step-Index")
                           @DefaultValue("0") int executionStep,
                           @Suspended AsyncResponse asyncResponse,
                           @Context HttpServletResponse response) {
        JsonNode app = createObjectNode()
                .put("application", input.getApplicationName())
                .set("parts", createArrayNode().add(createObjectNode()
                    .set("capabilities", createArrayNode().add(createObjectNode()
                        .put("module", "import")
                        .set("props", createObjectNode()
                                .put("gitImportUrl", input.getGitImportUrl())
                                .put("gitImportBranch", input.getGitImportBranch())
                                .put("builderImage", input.getBuilderImage())
                        )
                    ))
                ));
        DeploymentDescriptor deployment = toDescriptor(app);
        return performLaunch(deployment, input, executionStep, asyncResponse, response);
    }

    private DeploymentDescriptor toDescriptor(JsonNode json) {
        ObjectNode app = createObjectNode();
        app.set("applications", createArrayNode().add(json));
        Map<String, Object> project = JsonUtils.toMap(app);
        return DeploymentDescriptor.Companion.build(project);
    }

    private Response performLaunch(
            DeploymentDescriptor deployment,
            CreatorLaunchingProjectileContext input,
            int executionStep,
            AsyncResponse asyncResponse,
            HttpServletResponse response) {
        return ApplyKt.withDeployment(deployment, projectLocation -> {
            // Run the preparers on top of the uploaded code
            preparers.forEach(preparer -> preparer.prepare(projectLocation, null, input));
            CreateProjectile projectile = ImmutableLauncherCreateProjectile.builder()
                    .projectLocation(projectLocation)
                    .eventConsumer(eventBroker::send)
                    .gitOrganization(StringUtils.isBlank(input.getGitOrganization()) ? null :
                                             input.getGitOrganization())
                    .gitRepositoryName(input.getGitRepository())
                    .startOfStep(executionStep)
                    .openShiftProjectName(input.getProjectName())
                    .build();
            Collection<StatusEventKind> events =
                    projectile.getGitRepositoryName() == null ?
                            asList(LauncherStatusEventKind.OPENSHIFT_CREATE, LauncherStatusEventKind.OPENSHIFT_PIPELINE) :
                            asList(LauncherStatusEventKind.values());
            try {
                doLaunch(projectile, missionControl::launch, events, response, asyncResponse);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return Response.ok().build();
        });
    }
}
