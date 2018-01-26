package io.fabric8.launcher.web.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.fabric8.forge.generator.keycloak.KeycloakClient;
import io.fabric8.forge.generator.keycloak.KeycloakEndpoint;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;

/**
 * Used in OSIO only
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/service/github/update-github-webhook")
@ApplicationScoped
public class UpdateGitHubWebHooksResource {

    @Inject
    GitHubServiceFactory gitHubServiceFactory;


    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateWebHook(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
                                  @QueryParam("repository") String repository,
                                  @QueryParam("oldUrl") String oldUrl,
                                  @QueryParam("newUrl") String newUrl) throws MalformedURLException {
        String gitHubToken = new KeycloakClient().getTokenFor(KeycloakEndpoint.GET_GITHUB_TOKEN, authHeader);
        GitHubService gitHub = gitHubServiceFactory.create(IdentityFactory.createFromToken(gitHubToken));
        GitRepository gitRepository = ImmutableGitRepository.builder().fullName(repository).homepage(URI.create("http://foo")).gitCloneUri(URI.create("http://foo")).build();
        List<GitHook> hooks = gitHub.getHooks(gitRepository);
        for (GitHook hook : hooks) {
            if (Objects.equals(oldUrl, hook.getUrl())) {
                String[] events = hook.getEvents().toArray(new String[0]);
                GitHook newHook = gitHub.createHook(gitRepository, URI.create(newUrl).toURL(), events);
                gitHub.deleteWebhook(gitRepository, hook);
                return Response.created(URI.create(newHook.getUrl())).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}