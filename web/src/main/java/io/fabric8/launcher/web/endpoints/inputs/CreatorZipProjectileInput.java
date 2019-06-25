package io.fabric8.launcher.web.endpoints.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.core.api.projectiles.context.CreatorZipProjectileContext;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;

public class CreatorZipProjectileInput implements CreatorZipProjectileContext {

    @FormParam("project")
    private JsonNode project;

    @HeaderParam("X-Execution-Step-Index")
    @DefaultValue("0")
    private String step;

    @Override
    public JsonNode getProject() {
        return project;
    }

    public int getExecutionStep() {
        try {
            return Integer.parseInt(step);
        } catch (Exception e) {
            return 0;
        }
    }
}
