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

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.api.Identities;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

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
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry clusterRegistry;

    @Inject
    private Identities identities;

    @HEAD
    @Path("/repository/{repo}")
    public Response repositoryExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                     @NotNull @PathParam("repo") String repository) {
        Identity identity = identities.getGitHubIdentity(authorization);
        GitHubService gitHubService = gitHubServiceFactory.create(identity);
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

        Identity identity = identities.getOpenShiftIdentity(authorization, cluster);
        OpenShiftCluster openShiftCluster = clusterRegistry.findClusterById(cluster)
                .orElseThrow(() -> new IllegalStateException("Cluster not found"));
        OpenShiftService openShiftService = openShiftServiceFactory.create(openShiftCluster, identity);
        if (openShiftService.projectExists(project)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/token/openshift")
    public Response openShiftTokenExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                         @QueryParam("cluster") String cluster) {
        Identity identity = identities.getOpenShiftIdentity(authorization, cluster);
        boolean tokenExists = (identity != null);
        if (tokenExists) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/token/github")
    public Response gitHubTokenExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {
        Identity identity = identities.getGitHubIdentity(authorization);
        boolean tokenExists = (identity != null);
        if (tokenExists) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}