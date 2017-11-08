package io.openshift.appdev.missioncontrol.service.openshift.api;

/**
 * An OpenShift resource.
 * OpenShift resources belong to an {@link OpenShiftProject}, have a name and a kind
 */
public interface OpenShiftResource {

    /**
     * @return the name of this resource
     */
    String getName();

    /**
     * @return the kind of this resource, as returned in the OpenShift API response messages.
     *
     * <p>Example of values:
     * <ul>
     * <li>{@code Service}</li>
     * <li>{@code Route}</li>
     * <li>{@code BuildConfig}</li>
     * <li>{@code Pod}</li>
     * <li>{@code ImageStreamMapping}</li>
     * <li>etc.</li>
     * </ul></p>
     * @see <a href="https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/index.html"> OpenShift Architecture - Core concepts overview</a>
     */
    String getKind();

    /**
     * Returns the GitHub webhook secret,
     * if this is a {@code BuildConfig} resource of type GitHub, else null
     * @return
     */
    String getGitHubWebhookSecret();

    /**
     * @return the parent {@link OpenShiftProject} of this OpenShift resource
     */
    OpenShiftProject getProject();

}
