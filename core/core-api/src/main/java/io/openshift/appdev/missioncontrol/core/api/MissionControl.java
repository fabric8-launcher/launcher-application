package io.openshift.appdev.missioncontrol.core.api;

/**
 * Core API and entry point to the MissionControl.  Defines high-level
 * capabilities intended to be called by outside clients; designed to
 * be additionally exposed as a RESTful endpoint.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface MissionControl {

    /**
     * The {@link MissionControl}, as the name suggests, is a launcher.  Its responsibility
     * is to take the following inputs:
     * <ul>
     * <li>A GitHub project</li>
     * <li>A GitHub user (OAuth token)</li>
     * <li>An OpenShift user (OAuth token)</li>
     * <li>An OpenShift instance's API URL</li>
     * <li>An OpenShift user</li>
     * </ul>
     *
     * And perform the following actions:
     * <ul>
     * <li>Fork the GitHub project into the GitHub user's namespace</li>
     * <li>Create an OpenShift project</li>
     * <li>Apply the pipeline template to the OpenShift project</li>
     * <li>Associate the OpenShift project with the newly-forked GitHub repo</li>
     * <li>Create a GitHub webhook on the newly-forked GitHub project to
     * register push events to the OpenShift project</li>
     * </ul>
     *
     * This will result in a fully-pipelined OpenShift project from a source GitHub repo.
     * The pipeline definition itself is expected to reside in a Groovy-based
     * Jenkins Pipeline (https://github.com/jenkinsci/workflow-plugin/blob/master/README.md#introduction)
     * script called a Jenkinsfile.
     *
     * This project launching process done by the {@link MissionControl} is called a {@link MissionControl#launch(ForkProjectile)}.
     * All inputs are encapsulated inside a {@link Projectile}.  The returned result is,
     * quite unsurprisingly, a {@link Boom}, which contains all information relevant to the caller.
     *
     * @param projectile
     * @return The result of the operation encapsulated in a {@link Boom}
     * @throws IllegalArgumentException If the {@link Projectile} is not specified
     */
    Boom launch(final ForkProjectile projectile) throws IllegalArgumentException;


    /**
     * The {@link MissionControl}, as the name suggests, is a launcher.  Its responsibility
     * is to take the following inputs:
     * <ul>
     * <li>A location of the code</li>
     * <li>A GitHub user (OAuth token)</li>
     * <li>An OpenShift user (OAuth token)</li>
     * <li>An OpenShift instance's API URL</li>
     * <li>An OpenShift user</li>
     * </ul>
     *
     * And perform the following actions:
     * <ul>
     * <li>Create the GitHub project into the GitHub user's namespace and push the code</li>
     * <li>Create an OpenShift project</li>
     * <li>Apply the pipeline template to the OpenShift project</li>
     * <li>Associate the OpenShift project with the newly-forked GitHub repo</li>
     * <li>Create a GitHub webhook on the newly-forked GitHub project to
     * register push events to the OpenShift project</li>
     * </ul>
     *
     * This will result in a fully-pipelined OpenShift project from a source GitHub repo.
     * The pipeline definition itself is expected to reside in a Groovy-based
     * Jenkins Pipeline (https://github.com/jenkinsci/workflow-plugin/blob/master/README.md#introduction)
     * script called a Jenkinsfile.
     *
     * This project launching process done by the {@link MissionControl} is called a {@link MissionControl#launch(CreateProjectile)}.
     * All inputs are encapsulated inside a {@link Projectile}.  The returned result is,
     * quite unsurprisingly, a {@link Boom}, which contains all information relevant to the caller.
     *
     * @param projectile value object
     * @return The result of the operation encapsulated in a {@link Boom}
     * @throws IllegalArgumentException If the {@link Projectile} is not specified
     */
    Boom launch(final CreateProjectile projectile) throws IllegalArgumentException;
}
