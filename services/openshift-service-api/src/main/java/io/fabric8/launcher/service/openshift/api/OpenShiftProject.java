package io.fabric8.launcher.service.openshift.api;

import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

/**
 * Represents a Project in OpenShift
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Value.Immutable
public interface OpenShiftProject {

    /**
     * @return the name of this project
     */
    String getName();

    /**
     * @return the URL of the console overview page for this project
     */
    @Nullable
    URL getConsoleOverviewUrl();

    /**
     * @return an unmodifiable copy of the list of {@link OpenShiftResource} for this project
     */
    @Nullable
    List<OpenShiftResource> getResources();
}
