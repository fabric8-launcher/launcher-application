package io.fabric8.launcher.web.endpoints;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.creator.catalog.capabilities.CapabilityInfo;
import io.fabric8.launcher.creator.catalog.generators.GeneratorInfo;
import io.fabric8.launcher.creator.core.analysis.AnalyzeKt;
import io.fabric8.launcher.creator.core.analysis.GitKt;
import io.fabric8.launcher.creator.core.catalog.EnumsKt;
import io.fabric8.launcher.creator.core.resource.BuilderImage;
import io.fabric8.launcher.creator.core.resource.ImagesKt;

import javax.enterprise.context.RequestScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.fabric8.launcher.base.JsonUtils.*;

@Path("/creator")
@RequestScoped
public class CreatorEndpoint {

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
    public Response getEnums(@NotNull(message = "enum ID is missing") @PathParam("id") String id) {
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
    public Response getGitBranches(@NotNull(message = "gitImportUrl is required")
                                       @QueryParam("gitImportUrl") String gitImportUrl) {
        try {
            return Response.ok(GitKt.listBranchesFromGit(gitImportUrl)).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/import/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnalysis(@NotNull(message = "gitImportUrl is required") @QueryParam("gitImportUrl") String gitImportUrl, @QueryParam("gitImportBranch") String gitImportBranch) {
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
}
