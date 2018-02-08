package io.fabric8.launcher.web.api;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(ValidationResource.PATH_RESOURCE)
@ApplicationScoped
@Deprecated
public class ValidationResource {

    /**
     * Paths
     **/
    static final String PATH_RESOURCE = "/validate";

    @Inject
    private Instance<GitService> gitHubService;

    @Inject
    private Instance<OpenShiftService> openShiftService;

    @Inject
    @Application(Application.ApplicationType.LAUNCHER)
    private IdentityProvider identityProvider;

    @HEAD
    @Path("/repository/{repo}")
    public Response repositoryExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                     @NotNull @PathParam("repo") String repository) {
        if (gitHubService.get().getRepository(repository).isPresent()) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/project/{project}")
    public Response openShiftProjectExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                           @NotNull @PathParam("project") String project) {
        if (openShiftService.get().projectExists(project)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/token/openshift")
    public Response openShiftTokenExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                         @QueryParam("cluster") String cluster) {
        boolean tokenExists = identityProvider
                .getIdentity(Objects.toString(cluster, IdentityProvider.ServiceType.OPENSHIFT), authorization).isPresent();
        if (tokenExists) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/token/github")
    public Response gitHubTokenExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {
        boolean tokenExists = identityProvider.getIdentity(IdentityProvider.ServiceType.GITHUB, authorization).isPresent();
        if (tokenExists) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}