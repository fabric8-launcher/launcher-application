package io.openshift.appdev.missioncontrol.core.api;

/**
 * Value object defining the inputs to {@link MissionControl#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * ForkProjectile adds the values to Projectile that are needed to create a fork
 * of a project.
 */
public class ForkProjectile extends Projectile {

    /**
     * Package-level access; to be invoked by {@link ProjectileBuilder}
     * and all precondition checks are its responsibility
     *
     * @param builder
     */
    ForkProjectile(ForkProjectileBuilder builder) {
        super(builder);
        this.sourceGitHubRepo = builder.getSourceGitHubRepo();
        this.pipelineTemplatePath = builder.getPipelineTemplatePath();
        this.gitRef = builder.getGitRef();
    }

    private final String sourceGitHubRepo;

    private final String gitRef;

    /**
     * the path to the file in the repo that contains the pipeline template.
     */
    private String pipelineTemplatePath;

    /**
     * @return source GitHub repository name in form "owner/repoName".
     */
    public String getSourceGitHubRepo() {
        return this.sourceGitHubRepo;
    }

    /**
     * @return The path to the pipeline template file in the repo
     */
    public String getPipelineTemplatePath() {
        return pipelineTemplatePath;
    }

    /**
     * @return The Git reference to use
     */
    public String getGitRef() {
        return gitRef;
    }
}
