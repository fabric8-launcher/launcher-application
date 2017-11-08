package io.openshift.appdev.missioncontrol.service.openshift.api;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Holds a registry of supported OpenShift clusters
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OpenShiftClusterRegistry {

    /**
     * The supported clusters in this environment
     *
     * @return a {@link Set} of {@link OpenShiftCluster} objects
     */
    Set<OpenShiftCluster> getClusters();

    /**
     * @return the default {@link OpenShiftCluster} configuration
     */
    OpenShiftCluster getDefault();

    /**
     * Find an {@link OpenShiftCluster} by its id
     *
     * @param id the desired {@link OpenShiftCluster} id. Returns {@link OpenShiftClusterRegistry#getDefault()} if null
     * @return an {@link Optional} with the possible {@link OpenShiftCluster}
     */
    default Optional<OpenShiftCluster> findClusterById(final String id) {
        if (id == null) {
            return Optional.of(getDefault());
        }
        return getClusters()
                .stream()
                .filter(c -> Objects.equals(c.getId(), id))
                .findFirst();
    }

}
