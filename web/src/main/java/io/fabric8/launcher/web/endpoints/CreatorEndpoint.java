package io.fabric8.launcher.web.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.core.api.DefaultMissionControl;
import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.events.StatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.core.api.projectiles.ImmutableLauncherCreateProjectile;
import io.fabric8.launcher.core.api.projectiles.context.CreatorLauncherProjectileContext;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo;
import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo;
import io.fabric8.launcher.creator.core.analysis.AnalyzeKt;
import io.fabric8.launcher.creator.core.analysis.GitKt;
import io.fabric8.launcher.creator.core.catalog.EnumsKt;
import io.fabric8.launcher.creator.core.deploy.ApplyKt;
import io.fabric8.launcher.creator.core.deploy.DeploymentDescriptor;
import io.fabric8.launcher.creator.core.resource.BuilderImage;
import io.fabric8.launcher.creator.core.resource.ImagesKt;
import io.fabric8.launcher.web.endpoints.inputs.CreatorLaunchProjectileInput;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.fabric8.launcher.base.JsonUtils.*;
import static java.util.Arrays.asList;

@Path("/creator")
@RequestScoped
public class CreatorEndpoint extends AbstractLaunchEndpoint {

    @Inject
    DefaultMissionControl missionControl;

    @Inject
    StatusMessageEventBroker eventBroker;

    @Inject
    Instance<ProjectilePreparer> preparers;

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
        return Response.ok(GeneratorInfo.Companion.getInfos()).build();
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
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response zip(@BeanParam CreatorLaunchProjectileInput input) {
        Map<String, Object> project = JsonUtils.toMap(input.getProject());
        DeploymentDescriptor desc = DeploymentDescriptor.Companion.build(project);
        return ApplyKt.withDeployment(desc, projectLocation -> {
            final ObjectNode result = createObjectNode();
            int downloadId = 1;
            result.put("id", downloadId);
            // TODO: implement the actual caching of the zip
            return Response.ok(result).build();
        });
    }

    @GET
    @Path("/download/{id}")
    @Produces("application/zip")
    public Response getDownload(@NotNull(message = "download 'id' is required") @PathParam("id") String id) {
        // TODO return cached zip
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @POST
    @Path("/launch")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response launch(@Valid @BeanParam CreatorLaunchProjectileInput input,
                           @HeaderParam("X-Execution-Step-Index")
                           @DefaultValue("0") int executionStep,
                           @Suspended AsyncResponse asyncResponse,
                           @Context HttpServletResponse response) throws IOException {
        Map<String, Object> project = JsonUtils.toMap(input.getProject());
        DeploymentDescriptor desc = DeploymentDescriptor.Companion.build(project);
        return performLaunch(desc, input, executionStep, asyncResponse, response);
    }

    private Response performLaunch(
            DeploymentDescriptor deployment,
            CreatorLauncherProjectileContext input,
            int executionStep,
            AsyncResponse asyncResponse,
            HttpServletResponse response) {
        try {
            return ApplyKt.withDeployment(deployment, projectLocation -> {
                // Run the preparers on top of the uploaded code
                preparers.forEach(preparer -> preparer.prepare(projectLocation, null, input));
                CreateProjectile projectile = ImmutableLauncherCreateProjectile.builder()
                        .projectLocation(projectLocation)
                        .eventConsumer(eventBroker::send)
                        .gitOrganization(input.getGitOrganization())
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
                    throw new RuntimeException(ex);
                }
                return Response.ok().build();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
