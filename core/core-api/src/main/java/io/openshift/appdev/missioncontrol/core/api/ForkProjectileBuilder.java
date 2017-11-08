package io.openshift.appdev.missioncontrol.core.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.openshift.appdev.missioncontrol.base.identity.Identity;

/**
 * DSL builder for creating {@link ForkProjectile} objects.  Responsible for
 * validating state before calling upon the {@link ForkProjectileBuilder#build()}
 * operation.  The following properties are required:
 * <p>
 * <ul>
 * <li>sourceGitHubRepo</li>
 * </ul>
 * <p>
 * Each property's valid value and purpose is documented in its setter method.
 */
public class ForkProjectileBuilder extends ProjectileBuilder {
    ForkProjectileBuilder(Identity gitHubAccessToken, Identity openshiftAccessToken, String openShiftProjectName, String openShiftClusterName) {
        super(gitHubAccessToken, openshiftAccessToken, openShiftProjectName, openShiftClusterName);
    }

    private static final Pattern REPO_PATTERN = Pattern.compile("^[a-zA-Z_0-9\\-]+/[a-zA-Z_0-9\\-]+");

    private String sourceGitHubRepo;

    /**
     * the path to the file in the repo that contains the pipeline template.
     */
    private String pipelineTemplatePath;

    private String gitRef;

    /**
     * Creates and returns a new {@link ForkProjectile} instance based on the
     * state of this builder; if any preconditions like missing properties
     * or improper values exist, an {@link IllegalStateException} will be thrown
     *
     * @return the created {@link Projectile}
     * @throws IllegalStateException
     */
    public ForkProjectile build() throws IllegalStateException {
        super.build(this);
        // Precondition checks
        checkSpecified("sourceGitHubRepo", this.sourceGitHubRepo);
        final Matcher matcher = REPO_PATTERN.matcher(sourceGitHubRepo);
        if (!matcher.matches()) {
            throw new IllegalStateException("source repo must be in form \"owner/repoName\"");
        }
        checkSpecified("pipelineTemplatePath", this.pipelineTemplatePath);
        checkSpecified("girRef", this.gitRef);

        // All good, so make a new instance
        return new ForkProjectile(this);
    }

    /**
     * Sets the source GitHub repository name in form "owner/repoName"; this
     * is what will be forked on behalf of the user.  Required.
     *
     * @param sourceGitHubRepo
     * @return This builder
     */
    public ForkProjectileBuilder sourceGitHubRepo(final String sourceGitHubRepo) {
        this.sourceGitHubRepo = sourceGitHubRepo;
        return this;
    }

    /**
     * Sets the path to file that contains the template to apply on the
     * OpenShift project. Required.
     *
     * @param pipelineTemplatePath
     * @return This builder
     */
    public ForkProjectileBuilder pipelineTemplatePath(final String pipelineTemplatePath) {
        this.pipelineTemplatePath = pipelineTemplatePath;
        return this;
    }

    /**
     * Sets Git ref to use. Required
     *
     * @param gitRef
     * @return This builder
     */
    public ForkProjectileBuilder gitRef(final String gitRef) {
        this.gitRef = gitRef;
        return this;
    }

    /**
     * @return source GitHub repository name in form "owner/repoName".
     */
    public String getSourceGitHubRepo() {
        return this.sourceGitHubRepo;
    }

    /**
     * @return the path to the file that contains the template to apply on the OpenShift project.
     */
    public String getPipelineTemplatePath() {
        return this.pipelineTemplatePath;
    }

    /**
     * @return The Git reference to use
     */
    public String getGitRef() {
        return gitRef;
    }

    @Override
    String createDefaultProjectName() {
        final String sourceGitHubRepo = this.getSourceGitHubRepo();
        final String targetProjectName = this.getSourceGitHubRepo().substring(
                sourceGitHubRepo.lastIndexOf('/') + 1);

        return targetProjectName;
    }
}
