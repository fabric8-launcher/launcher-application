package io.fabric8.launcher.web.providers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.Application;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class ApplicationProducer {
    private static final String APP_HEADER = "X-App";

    private static final String DEFAULT_APP = "fabric8-launcher";

    @Produces
    @RequestScoped
    Application getApplication(HttpServletRequest request) {
        // If X-App is not specified, assume fabric8-launcher
        return of(Objects.toString(request.getHeader(APP_HEADER), DEFAULT_APP));
    }

}
