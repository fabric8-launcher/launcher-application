package io.fabric8.launcher.web.endpoints;

import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;

import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/git")
@RequestScoped
public class GitEndpoint {

    @Inject
    private Instance<GitServiceFactory> gitServiceFactories;

    @Inject
    private GitService gitService;

    @GET
    @Path("/providers")
    @Produces(MediaType.APPLICATION_JSON)
    public GitProviderType[] getProviders() {
        return GitProviderType.values();
    }


    @GET
    @Path("/user")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public GitUser getUser() {
        return gitService.getLoggedUser();
    }


    @GET
    @Path("/organizations")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getOrganizations() {
        return gitService.getOrganizations().stream()
                .map(GitOrganization::getName)
                .collect(Collectors.toSet());
    }

    @GET
    @Path("/repositories")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getRepositories(@QueryParam("organization") String organization) {
        ImmutableGitOrganization org = organization != null ? ImmutableGitOrganization.of(organization) : null;
        return gitService.getRepositories(org).stream()
                .map(GitRepository::getFullName)
                .collect(Collectors.toSet());
    }

    @HEAD
    @Secured
    @Path("/repositories/{repo}")
    public Response repositoryExists(@NotNull @PathParam("repo") String repository) {
        if (gitService.getRepository(repository).isPresent()) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
