/**
 * Copyright 2005-2015 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.jboss.obsidian.generator.rest;

import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.ui.command.CommandFactory;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContextListener;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.controller.CommandControllerFactory;
import org.jboss.obsidian.generator.spi.ResourceProvider;
import org.jboss.obsidian.generator.ui.RestUIContext;
import org.jboss.obsidian.generator.ui.RestUIRuntime;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.Collections;

import static javax.json.Json.createObjectBuilder;

@Path("/forge")
public class ObsidianResource {

    @Inject
    private CommandFactory commandFactory;

    @Inject
    private CommandControllerFactory controllerFactory;

    @Inject
    private ResourceProvider resourceProvider;

    @Inject
    private Iterable<UIContextListener> contextListeners;

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getInfo() {
        return createObjectBuilder()
                .add("version", "1.0")
                .build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getCommandInfo()
            throws Exception {
        JsonObjectBuilder builder = createObjectBuilder();
        return builder.build();
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject validateCommand(JsonObject content)
            throws Exception {
        JsonObjectBuilder builder = createObjectBuilder();
        return builder.build();
    }

    @POST
    @Path("/next")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject nextStep(JsonObject content)
            throws Exception {
        JsonObjectBuilder builder = createObjectBuilder();
        return builder.build();
    }

    @POST
    @Path("/execute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject executeCommand(JsonObject content)
            throws Exception {
        JsonObjectBuilder builder = createObjectBuilder();
        return builder.build();
    }

    private CommandController getObsidianCommand() throws Exception {
        RestUIContext context = createUIContext();
        // As the name is shellified, it needs to be false
        context.getProvider().setGUI(false);
        UICommand command = commandFactory.getCommandByName(context, "obsidian");
        context.getProvider().setGUI(true);
        CommandController controller = controllerFactory.createController(context,
                new RestUIRuntime(Collections.emptyList()), command);
        controller.initialize();
        return controller;
    }

    private RestUIContext createUIContext()
    {
        Resource<?> selection = resourceProvider.toResource("");
        return new RestUIContext(selection, contextListeners);
    }
}
