package io.fabric8.launcher.osio;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Just for testing as 'normally' the one from web will be used.
 */
@ApplicationPath("/api")
public class HttpApplication extends Application {
}
