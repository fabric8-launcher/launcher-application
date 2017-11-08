package io.openshift.appdev.missioncontrol.core.api;

import java.nio.file.Path;

import io.openshift.appdev.missioncontrol.base.identity.Identity;


/**
 * DSL builder for creating {@link CreateProjectile} objects.  Responsible for
 * validating state before calling upon the {@link CreateProjectileBuilder#build()}
 * operation.  The following properties are required:
 * <p>
 * <ul>
 * <li>gitHubIdentity</li>
 * <li>openShiftIdentity</li>
 * <li>projectLocation</li>
 * </ul>
 * <p>
 * Each property's valid value and purpose is documented in its setter method.
 */
public class CreateProjectileBuilder extends ProjectileBuilder {
    CreateProjectileBuilder(Identity gitHubIdentity, Identity openShiftIdentity, String openShiftProjectName, String openShiftClusterName) {
        super(gitHubIdentity, openShiftIdentity, openShiftProjectName, openShiftClusterName);
    }

    private Path projectLocation;

    /**
     * The GitHub Repository Name to be used
     */
    private String gitHubRepositoryName;

    /**
     * The Github Repository Description
     */
    private String gitHubRepositoryDescription = " ";

    /**
     * The name of the Mission
     */
    private String mission;

    /**
     * The name of the Runtime
     */
    private String runtime;

    /**
     * Creates and returns a new {@link CreateProjectile} instance based on the
     * state of this builder; if any preconditions like missing properties
     * or improper values exist, an {@link IllegalStateException} will be thrown
     *
     * @return the created {@link Projectile}
     * @throws IllegalStateException
     */
    public CreateProjectile build() {
        super.build(this);
        checkSpecified("projectLocation", this.projectLocation);
        return new CreateProjectile(this);
    }

    /**
     * Sets the projectLocation of the repository this
     * is what will be "uploaded" for the user.  Required.
     *
     * @param projectLocation
     * @return This builder
     */
    public CreateProjectileBuilder projectLocation(final Path projectLocation) {
        this.projectLocation = projectLocation;
        return this;
    }

    /**
     * @return the location of the project to "upload" to GitHub.
     */
    public Path getProjectLocation() {
        return projectLocation;
    }

    /**
     * Sets the GitHub repository name when creating a new repository
     *
     * @param gitHubRepositoryName
     * @return
     */
    public CreateProjectileBuilder gitHubRepositoryName(final String gitHubRepositoryName) {
        this.gitHubRepositoryName = gitHubRepositoryName;
        return this;
    }

    public String getGitHubRepositoryName() {
        return gitHubRepositoryName;
    }

    /**
     * Sets the GitHub repository description when creating a new repository
     *
     * @param description
     * @return
     */
    public CreateProjectileBuilder gitHubRepositoryDescription(final String description) {
        this.gitHubRepositoryDescription = description;
        return this;
    }


    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }

    /**
     * Sets the name of the mission of the booster associated with this builder
     *
     * @param mission
     * @return
     */
    public CreateProjectileBuilder mission(String mission) {
        this.mission = mission;
        return this;
    }

    public String getMission() {
        return mission;
    }

    /**
     * Sets the name of the runtime of the booster associated with this builder
     *
     * @param runtime
     * @return
     */
    public CreateProjectileBuilder runtime(String runtime) {
        this.runtime = runtime;
        return this;
    }

    public String getRuntime() {
        return runtime;
    }

    @Override
    String createDefaultProjectName() {
        return projectLocation.getFileName().toString();
    }
}
