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
package io.obsidian.generator.rest;

import static javax.json.Json.createObjectBuilder;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.command.CommandFactory;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.controller.CommandControllerFactory;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.forge.service.ui.RestUIContext;
import org.jboss.forge.service.ui.RestUIRuntime;
import org.jboss.forge.service.util.UICommandHelper;

import io.obsidian.generator.ForgeInitializer;
import io.obsidian.generator.util.JsonBuilder;

@Path("/forge")
public class ObsidianResource
{

   private static final String OBSIDIAN_COMMAND_NAME = "Obsidian: New Project";

   @Inject
   private CommandFactory commandFactory;

   @Inject
   private CommandControllerFactory controllerFactory;

   @Inject
   private ResourceFactory resourceFactory;

   @Inject
   private UICommandHelper helper;

   @GET
   @Path("/version")
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject getInfo()
   {
      return createObjectBuilder()
               .add("version", Versions.getImplementationVersionFor(UIContext.class).toString())
               .build();
   }

   @GET
   @Path("/")
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject getCommandInfo() throws Exception
   {
      JsonObjectBuilder builder = createObjectBuilder();
      try (CommandController controller = getObsidianCommand())
      {
         helper.describeController(builder, controller);
      }
      return builder.build();
   }

   @POST
   @Path("/validate")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject validateCommand(JsonObject content)
            throws Exception
   {
      JsonObjectBuilder builder = createObjectBuilder();
      try (CommandController controller = getObsidianCommand())
      {
         helper.populateControllerAllInputs(content, controller);
         helper.describeCurrentState(builder, controller);
         helper.describeValidation(builder, controller);
         helper.describeInputs(builder, controller);
      }
      return builder.build();
   }

   @POST
   @Path("/next")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject nextStep(JsonObject content)
            throws Exception
   {
      int stepIndex = content.getInt("stepIndex", 1);
      JsonObjectBuilder builder = createObjectBuilder();
      try (CommandController controller = getObsidianCommand())
      {
         if (!(controller instanceof WizardCommandController))
         {
            throw new WebApplicationException("Controller is not a wizard", Status.BAD_REQUEST);
         }
         WizardCommandController wizardController = (WizardCommandController) controller;
         for (int i = 0; i < stepIndex; i++)
         {
            if (wizardController.canMoveToNextStep())
            {
               helper.populateController(content, wizardController);
               helper.describeValidation(builder, controller);
               wizardController.next().initialize();
            }
         }
         helper.describeMetadata(builder, controller);
         helper.describeCurrentState(builder, controller);
         helper.describeInputs(builder, controller);
      }
      return builder.build();
   }

   @POST
   @Path("/execute")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response executeCommand(JsonObject content)
            throws Exception
   {
      try (CommandController controller = getObsidianCommand())
      {
         helper.populateControllerAllInputs(content, controller);
         if (controller.isValid())
         {
            Result result = controller.execute();
            if (result instanceof Failed)
            {
               return Response.status(Status.INTERNAL_SERVER_ERROR).entity(result.getMessage()).build();
            }
            else
            {
               UISelection<?> selection = controller.getContext().getSelection();
               java.nio.file.Path path = Paths.get(selection.get().toString());
               String artifactId = findArtifactId(content);
               byte[] zipContents = io.obsidian.generator.util.Paths.zip(artifactId, path);
               io.obsidian.generator.util.Paths.deleteDirectory(path);
               return Response
                        .ok(zipContents)
                        .type("application/zip")
                        .header("Content-Disposition", "attachment; filename=\"" + artifactId + ".zip\"")
                        .build();
            }
         }
         else
         {
            JsonObjectBuilder builder = createObjectBuilder();
            helper.describeValidation(builder, controller);
            return Response.status(Status.PRECONDITION_FAILED).entity(builder.build()).build();
         }
      }
   }

   /**
    * @param content
    * @return
    */
   private String findArtifactId(JsonObject content)
   {
      return content.getJsonArray("inputs").stream()
               .map(item -> (JsonObject) item)
               .filter(input -> "named".equals(input.getString("name")))
               .map(input -> input.get("value").toString())
               .findFirst().orElse("demo");
   }

   @POST
   @Path("/execute")
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   public Response executeCommand(Form form) throws Exception
   {
      String stepIndex = form.asMap().remove("stepIndex").get(0);
      final JsonBuilder jsonBuilder = new JsonBuilder().createJson(Integer.valueOf(stepIndex));
      for (Map.Entry<String, List<String>> entry : form.asMap().entrySet())
      {
         jsonBuilder.addInput(entry.getKey(), entry.getValue());
      }

      final Response response = executeCommand(jsonBuilder.build());
      if (response.getEntity() instanceof JsonObject)
      {
         JsonObject responseEntity = (JsonObject) response.getEntity();
         String error = ((JsonObject) responseEntity.getJsonArray("messages").get(0)).getString("description");
         return Response.status(Status.PRECONDITION_FAILED).entity(error).build();
      }
      return response;
   }

   private CommandController getObsidianCommand() throws Exception
   {
      RestUIContext context = createUIContext();
      UICommand command = commandFactory.getCommandByName(context, OBSIDIAN_COMMAND_NAME);
      CommandController controller = controllerFactory.createController(context,
               new RestUIRuntime(Collections.emptyList()), command);
      controller.initialize();
      return controller;
   }

   private RestUIContext createUIContext()
   {
      java.nio.file.Path rootPath = ForgeInitializer.getRoot();
      Resource<?> selection = resourceFactory.create(rootPath.toFile());
      return new RestUIContext(selection, Collections.emptyList());
   }
}
