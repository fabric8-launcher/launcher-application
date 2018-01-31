package io.fabric8.launcher.web.endpoints;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.service.git.api.GitServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/git")
public class GitResource {

    @Inject
    private Instance<GitServiceFactory> gitServiceFactories;

    @GET
    @Path("/providers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getProviders() {
        return StreamSupport.stream(gitServiceFactories.spliterator(), false)
                .map(GitServiceFactory::getName)
                .collect(Collectors.toList());
    }
}
