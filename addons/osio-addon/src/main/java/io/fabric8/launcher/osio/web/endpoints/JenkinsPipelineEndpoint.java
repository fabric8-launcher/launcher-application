package io.fabric8.launcher.osio.web.endpoints;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.osio.jenkins.JenkinsPipeline;
import io.fabric8.launcher.osio.jenkins.JenkinsPipelineRegistry;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/jenkins/pipelines")
public class JenkinsPipelineEndpoint {

    @Inject
    private JenkinsPipelineRegistry service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JenkinsPipeline> getPipelines(@QueryParam("platform") String platform) {
        return service.getPipelines(platform);
    }
}
