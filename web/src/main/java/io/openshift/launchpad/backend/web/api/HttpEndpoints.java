package io.openshift.launchpad.backend.web.api;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.openshift.appdev.missioncontrol.web.api.MissionControlResource;
import io.openshift.appdev.missioncontrol.web.api.OpenShiftResource;
import io.openshift.appdev.missioncontrol.web.api.ValidationResource;
import io.openshift.launchpad.backend.rest.LaunchResource;

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

    @Inject
    private LaunchResource launchResource;

    @Inject
    private MissionControlResource missionControlResource;

    @Inject
    private ValidationResource userResource;

    @Inject
    private OpenShiftResource openShiftResource;

    @Inject
    private HealthResource healthResource;

    @Override
    public Set<Object> getSingletons() {
        final Set<Object> singletons = new HashSet<>();
        singletons.add(launchResource);
        singletons.add(missionControlResource);
        singletons.add(userResource);
        singletons.add(openShiftResource);
        singletons.add(healthResource);

        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setExposedHeaders("Content-Disposition");
        singletons.add(corsFilter);
        return singletons;
    }
}
