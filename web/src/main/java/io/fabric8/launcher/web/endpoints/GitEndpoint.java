package io.fabric8.launcher.web.endpoints;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;

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
    public List<String> getProviders() {
        return StreamSupport.stream(gitServiceFactories.spliterator(), false)
                .map(GitServiceFactory::getName)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/organizations")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getOrganizations() {
        return gitService.getOrganizations().stream()
                .map(GitOrganization::getName)
                .collect(Collectors.toSet());
    }

    @GET
    @Path("/repositories")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getRepositories(@QueryParam("organization") String organization) {
        ImmutableGitOrganization org = organization != null ? ImmutableGitOrganization.of(organization) : null;
        return gitService.getRepositories(org).stream()
                .map(GitRepository::getFullName)
                .collect(Collectors.toSet());
    }
}
