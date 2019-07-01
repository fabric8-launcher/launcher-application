package io.fabric8.launcher.web.endpoints.inputs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.core.api.projectiles.context.CreatorZipProjectileContext;

import javax.ws.rs.FormParam;

public class CreatorZipProjectileInput implements CreatorZipProjectileContext {

    @FormParam("project")
    private ObjectNode project;

    @Override
    public ObjectNode getProject() {
        return project;
    }
}
