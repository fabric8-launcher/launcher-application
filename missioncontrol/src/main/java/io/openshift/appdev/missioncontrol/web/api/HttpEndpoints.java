package io.openshift.appdev.missioncontrol.web.api;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;

/**
 * Defines our HTTP endpoints as singletons
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@ApplicationPath(HttpEndpoints.PATH_API)
public class HttpEndpoints extends Application {
    public static final String PATH_API = "/api";

    @Inject
    private MissionControlResource missionControlResource;

    @Inject
    private HealthResource healthResource;

    @Inject
    private ValidationResource userResource;

    @Inject
    private OpenShiftResource openShiftResource;

    @Override
    public Set<Object> getSingletons() {
        final Set<Object> singletons = new HashSet<>();
        singletons.add(missionControlResource);
        singletons.add(healthResource);
        singletons.add(userResource);
        singletons.add(openShiftResource);

        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setExposedHeaders("Content-Disposition");
        singletons.add(corsFilter);
        return singletons;
    }
}
