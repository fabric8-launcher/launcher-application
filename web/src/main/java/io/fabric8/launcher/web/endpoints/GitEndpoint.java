package io.fabric8.launcher.web.endpoints;

import java.util.List;
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
import io.fabric8.launcher.service.git.api.GitRepositoryFilter;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepositoryFilter;
import io.fabric8.launcher.web.endpoints.models.GitDetailedUser;
import io.fabric8.launcher.web.endpoints.models.ImmutableGitDetailedUser;

import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/git")
@RequestScoped
public class GitEndpoint {

    @Inject
    private Instance<GitService> gitService;

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
    public GitDetailedUser getUser() {
        return ImmutableGitDetailedUser.builder()
                .user(gitService.get().getLoggedUser())
                .organizations(getOrganizations())
                .repositories(getRepositories(null))
                .build();
    }

    @GET
    @Path("/organizations")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getOrganizations() {
        return gitService.get().getOrganizations().stream()
                .map(GitOrganization::getName)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/repositories")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getRepositories(@QueryParam("organization") String organization) {
        final GitRepositoryFilter filter = ImmutableGitRepositoryFilter.builder()
                .withOrganization(organization != null ? ImmutableGitOrganization.of(organization) : null)
                .build();
        return gitService.get().getRepositories(filter).stream()
                .map(GitRepository::getFullName)
                .collect(Collectors.toSet());
    }

    @HEAD
    @Secured
    @Path("/repositories/{repo}")
    public Response repositoryExists(@NotNull @PathParam("repo") String repository) {
        if (gitService.get().getRepository(repository).isPresent()) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
