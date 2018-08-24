package io.fabric8.launcher.web.endpoints.websocket;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.fabric8.launcher.core.api.events.LauncherStatusEventKind;
import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;

import static io.fabric8.launcher.web.endpoints.websocket.MissionControlStatusEndpointIT.EXTRA_DATA_KEY;
import static java.util.Collections.singletonMap;

/**
 * Websocket message listener for assertions.
 */
@ApplicationScoped
@Path("/test")
public class TestEventEndpoint {

    @Inject
    StatusMessageEventBroker broker;

    @POST
    @Path("/event/{uuid}")
    public void post(@PathParam("uuid") final String uuid, @FormParam("message") final String message) {
        broker.send(new StatusMessageEvent(UUID.fromString(uuid), LauncherStatusEventKind.GITHUB_CREATE,
                                           singletonMap(EXTRA_DATA_KEY, message)));
    }

}
