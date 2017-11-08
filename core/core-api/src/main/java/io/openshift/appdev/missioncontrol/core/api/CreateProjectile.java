package io.openshift.appdev.missioncontrol.core.api;

import java.nio.file.Path;

/**
 * Value object defining the inputs to {@link MissionControl#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * This projectile is used to create a project in the users github.
 */
public class CreateProjectile extends Projectile {
    /**
     * Package-level access; to be invoked by {@link ProjectileBuilder}
     * and all precondition checks are its responsibility
     *
     * @param builder
     */
    CreateProjectile(CreateProjectileBuilder builder) {
        super(builder);
        this.projectLocation = builder.getProjectLocation();
        this.gitHubRepositoryName = builder.getGitHubRepositoryName();
        this.gitHubRepositoryDescription = builder.getGitHubRepositoryDescription();
        this.mission = builder.getMission();
        this.runtime = builder.getRuntime();
    }

    private final Path projectLocation;
    private final String gitHubRepositoryName;
    private final String gitHubRepositoryDescription;
    private final String mission;
    private final String runtime;

    public Path getProjectLocation() {
        return projectLocation;
    }

    public String getGitHubRepositoryName() {
        return gitHubRepositoryName;
    }

    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }

    public String getMission() {
        return mission;
    }

    public String getRuntime() {
        return runtime;
    }
    
}
