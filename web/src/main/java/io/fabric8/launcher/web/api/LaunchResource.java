/**
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.launcher.web.api;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.booster.catalog.LauncherConfiguration;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import io.fabric8.launcher.web.forge.ForgeInitializer;
import io.fabric8.launcher.web.forge.util.JsonBuilder;
import io.fabric8.launcher.web.forge.util.Results;
import io.fabric8.utils.Strings;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.command.CommandFactory;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.controller.CommandControllerFactory;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.furnace.container.cdi.events.Local;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.forge.service.ui.RestUIContext;
import org.jboss.forge.service.ui.RestUIRuntime;
import org.jboss.forge.service.util.UICommandHelper;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

import static io.fabric8.launcher.web.forge.util.JsonOperations.exceptionToJson;
import static io.fabric8.launcher.web.forge.util.JsonOperations.unwrapJsonObjects;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @deprecated superceeded by {@link io.fabric8.launcher.web.endpoints.LaunchEndpoint}
 */
@javax.ws.rs.Path("/launchpad")
@ApplicationScoped
@Deprecated
public class LaunchResource {

    private static final String DEFAULT_COMMAND_NAME = "launchpad-new-project";

    private static final Logger log = Logger.getLogger(LaunchResource.class.getName());

    private static final String LAUNCHER_MISSIONCONTROL_SERVICE_HOST = "LAUNCHER_MISSIONCONTROL_SERVICE_HOST";

    private static final String LAUNCHER_MISSIONCONTROL_SERVICE_PORT = "LAUNCHER_MISSIONCONTROL_SERVICE_PORT";

    private final Map<String, String> commandMap = new TreeMap<>();

    private URI missionControlURI;

    @Inject
    private CommandFactory commandFactory;

    @Inject
    private CommandControllerFactory controllerFactory;

    @Inject
    private ResourceFactory resourceFactory;

    @Inject
    private UICommandHelper helper;

    @Inject
    private DirectoryReaper directoryReaper;

    public LaunchResource() {
        commandMap.put("launchpad-new-project", "Launchpad: New Project");
        commandMap.put("fabric8-new-project", "Fabric8: New Project");
        commandMap.put("fabric8-import-git", "fabric8: Import Git");
        commandMap.put("fabric8-check-git-accounts", "fabric8: Check Git Accounts");
        // TODO only enable if not using SaaS mode:
        commandMap.put("fabric8-configure-git-account", "fabric8: Configure Git Account");
    }

    void init(@Observes @Local PostStartup startup) {
        try {
            // Initialize Catapult URL
            initializeMissionControlServiceURI();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while warming up cache", e);
        }
    }

    @GET
    @javax.ws.rs.Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getInfo() {
        return createObjectBuilder()
                .add("forgeVersion", Versions.getImplementationVersionFor(UIContext.class).toString())
                .add("catalogRef", EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF, "next"))
                .build();
    }

    @GET
    @javax.ws.rs.Path("/commands/{commandName}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getCommandInfo(
            @PathParam("commandName") @DefaultValue(DEFAULT_COMMAND_NAME) String commandName,
            @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        JsonObjectBuilder builder = createObjectBuilder();
        try (CommandController controller = getCommand(commandName, ForgeInitializer.getRoot(), headers)) {
            helper.describeController(builder, controller);
        }
        return builder.build();
    }

    @POST
    @javax.ws.rs.Path("/commands/{commandName}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject validateCommand(JsonObject content,
                                      @PathParam("commandName") @DefaultValue(DEFAULT_COMMAND_NAME) String commandName,
                                      @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        JsonObjectBuilder builder = createObjectBuilder();
        try (CommandController controller = getCommand(commandName, ForgeInitializer.getRoot(), headers)) {
            controller.getContext().getAttributeMap().put("action", "validate");
            helper.populateController(content, controller);
            int stepIndex = content.getInt("stepIndex", 1);
            if (controller instanceof WizardCommandController) {
                WizardCommandController wizardController = (WizardCommandController) controller;
                for (int i = 0; i < stepIndex; i++) {
                    wizardController.next().initialize();
                    helper.populateController(content, wizardController);
                }
            }
            helper.describeValidation(builder, controller);
            helper.describeInputs(builder, controller);
            describeCurrentState(builder, controller);
        }
        return builder.build();
    }

    @POST
    @javax.ws.rs.Path("/commands/{commandName}/next")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject nextStep(JsonObject content,
                               @PathParam("commandName") @DefaultValue(DEFAULT_COMMAND_NAME) String commandName,
                               @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        int stepIndex = content.getInt("stepIndex", 1);
        JsonObjectBuilder builder = createObjectBuilder();
        try (CommandController controller = getCommand(commandName, ForgeInitializer.getRoot(), headers)) {
            if (!(controller instanceof WizardCommandController)) {
                throw new WebApplicationException("Controller is not a wizard", Status.BAD_REQUEST);
            }
            controller.getContext().getAttributeMap().put("action", "next");
            WizardCommandController wizardController = (WizardCommandController) controller;
            helper.populateController(content, controller);
            for (int i = 0; i < stepIndex; i++) {
                wizardController.next().initialize();
                helper.populateController(content, wizardController);
            }
            helper.describeMetadata(builder, controller);
            helper.describeInputs(builder, controller);
            describeCurrentState(builder, controller);
        }
        return builder.build();
    }

    @GET
    @javax.ws.rs.Path("/commands/{commandName}/query")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response executeQuery(@Context UriInfo uriInfo,
                                 @PathParam("commandName") String commandName,
                                 @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        String stepIndex = null;
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        List<String> stepValues = parameters.get("stepIndex");
        if (stepValues != null && !stepValues.isEmpty()) {
            stepIndex = stepValues.get(0);
        }
        if (stepIndex == null) {
            stepIndex = "0";
        }
        final JsonBuilder jsonBuilder = new JsonBuilder().createJson(Integer.valueOf(stepIndex));
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (!"stepIndex".equals(key)) {
                jsonBuilder.addInput(key, entry.getValue());
            }
        }

        final Response response = executeCommandJson(jsonBuilder.build(), commandName, headers);
        if (response.getEntity() instanceof JsonObject) {
            JsonObject responseEntity = (JsonObject) response.getEntity();
            String error = ((JsonObject) responseEntity.getJsonArray("messages").get(0)).getString("description");
            return Response.status(Status.PRECONDITION_FAILED).entity(unwrapJsonObjects(error)).build();
        }
        return response;
    }

    @POST
    @javax.ws.rs.Path("/commands/{commandName}/zip")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response downloadZip(Form form,
                                @PathParam("commandName") @DefaultValue(DEFAULT_COMMAND_NAME) String commandName,
                                @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        String stepIndex = form.asMap().remove("stepIndex").get(0);
        final JsonBuilder jsonBuilder = new JsonBuilder().createJson(Integer.parseInt(stepIndex));
        for (Map.Entry<String, List<String>> entry : form.asMap().entrySet()) {
            jsonBuilder.addInput(entry.getKey(), entry.getValue());
        }
        JsonObject content = jsonBuilder.build();
        Path path = Files.createTempDirectory("projectDir");
        try (CommandController controller = getCommand(commandName, path, headers)) {
            helper.populateControllerAllInputs(content, controller);
            if (controller.isValid()) {
                Result result = controller.execute();
                if (result instanceof Failed) {
                    return Response.serverError().entity(result.getMessage()).build();
                } else {
                    UISelection<?> selection = controller.getContext().getSelection();
                    Path projectPath = Paths.get(selection.get().toString());
                    // If downloading a zip, delete .openshiftio dir
                    Path openshiftIoPath = projectPath.resolve(".openshiftio");
                    if (Files.exists(openshiftIoPath)) {
                        io.fabric8.launcher.base.Paths.deleteDirectory(openshiftIoPath);
                    }
                    // Delete Jenkinsfile if exists
                    Files.deleteIfExists(projectPath.resolve("Jenkinsfile"));

                    String artifactId = Results.getEntityAsMap(result).getOrDefault("artifactId", "booster");
                    byte[] zipContents = io.fabric8.launcher.base.Paths.zip(artifactId, projectPath);
                    return Response
                            .ok(zipContents)
                            .type("application/zip")
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + artifactId + ".zip\"")
                            .build();
                }
            } else {
                JsonObjectBuilder builder = createObjectBuilder();
                helper.describeValidation(builder, controller);
                return Response.status(Status.PRECONDITION_FAILED).entity(builder.build()).build();
            }
        } finally {
            directoryReaper.delete(path);
        }
    }

    @POST
    @javax.ws.rs.Path("/commands/{commandName}/missioncontrol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadZip(JsonObject content,
                              @PathParam("commandName") @DefaultValue(DEFAULT_COMMAND_NAME) String commandName,
                              @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        Path path = Files.createTempDirectory("projectDir");
        try (CommandController controller = getCommand(commandName, path, headers)) {
            helper.populateControllerAllInputs(content, controller);
            if (controller.isValid()) {
                Result result = controller.execute();
                if (result instanceof Failed) {
                    return Response.serverError().entity(result.getMessage()).build();
                } else {
                    Map<String, String> returnMap = Results.getEntityAsMap(result);
                    UISelection<?> selection = controller.getContext().getSelection();
                    Path projectPath = Paths.get(selection.get().toString());
                    String artifactId = returnMap.getOrDefault("named", "booster");
                    byte[] zipContents = io.fabric8.launcher.base.Paths.zip(artifactId, projectPath);
                    Client client = ClientBuilder.newBuilder().build();
                    try {
                        WebTarget target = client.target(missionControlURI)
                                .property(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA);

                        // Create request body
                        MultipartFormDataOutput form = new MultipartFormDataOutput();
                        form.addFormData("file", new ByteArrayInputStream(zipContents),
                                         MediaType.MULTIPART_FORM_DATA_TYPE, "project.zip");

                        returnMap.forEach((k, v) -> {
                            if (v == null) {
                                log.warning("No data found for key " + k);
                            } else {
                                form.addFormData(k, v, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
                            }
                        });

                        // Execute POST Request
                        Response response = target.request()
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
                                .header("X-OpenShift-Cluster", returnMap.get("openShiftCluster"))
                                // Propagate Authorization header
                                .header(HttpHeaders.AUTHORIZATION, headers.getHeaderString(HttpHeaders.AUTHORIZATION))
                                .post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA_TYPE));
                        if (response.getStatus() == Status.OK.getStatusCode()) {
                            return Response.ok(response.readEntity(String.class), MediaType.APPLICATION_JSON).build();
                        } else {
                            return Response.status(response.getStatusInfo()).build();
                        }
                    } finally {
                        client.close();
                    }
                }
            } else {
                JsonObjectBuilder builder = createObjectBuilder();
                helper.describeValidation(builder, controller);
                return Response.status(Status.PRECONDITION_FAILED).entity(builder.build()).build();
            }
        } finally {
            directoryReaper.delete(path);
        }
    }

    @POST
    @javax.ws.rs.Path("/commands/{commandName}/execute")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeCommandJson(JsonObject content,
                                       @PathParam("commandName") @DefaultValue(DEFAULT_COMMAND_NAME) String commandName,
                                       @Context HttpHeaders headers)
            throws Exception {
        validateCommand(commandName);
        java.nio.file.Path path = Files.createTempDirectory("projectDir");
        try (CommandController controller = getCommand(commandName, path, headers)) {
            helper.populateControllerAllInputs(content, controller);
            if (controller.isValid()) {
                Result result = controller.execute();
                if (result instanceof Failed) {
                    JsonObjectBuilder builder = Json.createObjectBuilder();
                    helper.describeResult(builder, result);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(unwrapJsonObjects(builder)).build();
                } else {
                    Object entity = Results.getEntity(result);
                    if (entity != null) {
                        entity = unwrapJsonObjects(entity);
                        return Response
                                .ok(entity)
                                .type(MediaType.APPLICATION_JSON)
                                .build();
                    } else {
                        return Response
                                .ok(Results.getMessage(result))
                                .type(MediaType.TEXT_PLAIN)
                                .build();
                    }
                }
            } else {
                JsonObjectBuilder builder = createObjectBuilder();
                helper.describeValidation(builder, controller);
                return Response.status(Status.PRECONDITION_FAILED).entity(unwrapJsonObjects(builder.build())).build();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            JsonObject result = exceptionToJson(e, 7);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }
    }

    private void validateCommand(String commandName) {
        if (commandMap.get(commandName) == null) {
            String message = "No such command '" + commandName + "'. Supported commmands are '"
                    + String.join("', '", commandMap.keySet()) + "'";
            throw new WebApplicationException(message, Status.NOT_FOUND);
        }
    }

    private void initializeMissionControlServiceURI() {
        String host = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_SERVICE_HOST, "localhost");
        UriBuilder uri = UriBuilder.fromPath("/api/missioncontrol/upload").host(host).scheme("http");
        String port = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_SERVICE_PORT);
        uri.port(port != null ? Integer.parseInt(port) : 8080);
        missionControlURI = uri.build();
    }

    private CommandController getCommand(String name, Path initialPath, HttpHeaders headers) throws Exception {
        RestUIContext context = createUIContext(initialPath, headers);
        UICommand command = commandFactory.getNewCommandByName(context, commandMap.get(name));
        CommandController controller = controllerFactory.createController(context,
                                                                          new RestUIRuntime(Collections.emptyList()), command);
        controller.initialize();
        return controller;
    }

    private RestUIContext createUIContext(Path initialPath, HttpHeaders headers) {
        Resource<?> selection = resourceFactory.create(initialPath.toFile());
        RestUIContext context = new RestUIContext(selection, Collections.emptyList());
        if (headers != null) {
            Map<Object, Object> attributeMap = context.getAttributeMap();
            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            requestHeaders.keySet().forEach(key -> attributeMap.put(Strings.stripPrefix(key, "X-"), headers.getRequestHeader(key)));
        }
        return context;
    }

    private static void describeCurrentState(JsonObjectBuilder builder, CommandController controller) {
        JsonObjectBuilder stateBuilder = createObjectBuilder();
        stateBuilder.add("valid", controller.isValid());
        stateBuilder.add("canExecute", controller.canExecute());
        if (controller instanceof WizardCommandController) {
            stateBuilder.add("wizard", true);
            WizardCommandController wizardController = (WizardCommandController) controller;

            boolean canMoveToNextStep;
            try {
                canMoveToNextStep = wizardController.canMoveToNextStep();
            } catch (IndexOutOfBoundsException iob) {
                // Ignore for now
                canMoveToNextStep = false;
            }
            stateBuilder.add("canMoveToNextStep", canMoveToNextStep);

            boolean canMoveToPreviousStep;
            try {
                canMoveToPreviousStep = wizardController.canMoveToPreviousStep();
            } catch (IndexOutOfBoundsException iob) {
                // Ignore for now
                canMoveToPreviousStep = false;
            }
            stateBuilder.add("canMoveToPreviousStep", canMoveToPreviousStep);

            // Add flow
            JsonArrayBuilder wizardNamesStepBuilder = createArrayBuilder();
            List<UICommandMetadata> stepsMetadata = wizardController.getWizardStepsMetadata();
            for (UICommandMetadata metadata : stepsMetadata) {
                wizardNamesStepBuilder.add(metadata.getName());
            }
            stateBuilder.add("steps", wizardNamesStepBuilder);
        } else {
            stateBuilder.add("wizard", false);
        }
        builder.add("state", stateBuilder);
    }


}
