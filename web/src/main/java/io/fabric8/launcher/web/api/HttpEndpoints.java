package io.fabric8.launcher.web.api;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Defines our HTTP endpoints as singletons
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
@ApplicationPath(HttpEndpoints.PATH_API)
public class HttpEndpoints extends Application {
    public static final String PATH_API = "/api";
}
