package io.fabric8.launcher.osio.web.endpoints;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.osio.jenkins.JenkinsPipeline;
import io.fabric8.launcher.osio.jenkins.JenkinsPipelineRegistry;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/jenkins/pipelines")
@ApplicationScoped
public class JenkinsPipelineEndpoint {

    @Inject
    private JenkinsPipelineRegistry service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JenkinsPipeline> getPipelines(@QueryParam("platform") String platform) {
        return service.getPipelines(platform);
    }

    @GET
    @Path("/{id}/jenkinsfile")
    @Produces(MediaType.TEXT_PLAIN)
    public String getJenkinsFileContentPipeline(@PathParam("id") String pipelineId) throws IOException {
        JenkinsPipeline pipeline = service.findPipelineById(pipelineId)
                .orElseThrow(NotFoundException::new);
        return new String(Files.readAllBytes(pipeline.getJenkinsfilePath()));
    }
}
