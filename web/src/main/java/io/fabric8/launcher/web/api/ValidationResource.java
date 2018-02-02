package io.fabric8.launcher.web.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(ValidationResource.PATH_RESOURCE)
@ApplicationScoped
public class ValidationResource {

    /**
     * Paths
     **/
    static final String PATH_RESOURCE = "/validate";

    @Inject
    private GitService gitHubService;

    @Inject
    private OpenShiftService openShiftService;

    @HEAD
    @Path("/repository/{repo}")
    public Response repositoryExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                     @NotNull @PathParam("repo") String repository) {
        if (gitHubService.getRepository(repository).isPresent()) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/project/{project}")
    public Response openShiftProjectExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                           @NotNull @PathParam("project") String project,
                                           @QueryParam("cluster") String cluster) {
        if (openShiftService.projectExists(project)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}