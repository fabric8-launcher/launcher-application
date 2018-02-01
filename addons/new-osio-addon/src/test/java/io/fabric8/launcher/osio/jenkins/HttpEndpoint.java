package io.fabric8.launcher.osio.jenkins;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Just for testing as 'normally' the one from web will be used.
 */
@ApplicationPath("/api")
public class HttpEndpoint extends Application {
}
