package io.fabric8.launcher.osio.jenkins;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/services/jenkins/pipelines")
public class JenkinsPipelineResource {

    @Inject
    private JenkinsPipelineRegistry service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JenkinsPipeline> getPipelines() {
        return service.getPipelines();
    }
}
