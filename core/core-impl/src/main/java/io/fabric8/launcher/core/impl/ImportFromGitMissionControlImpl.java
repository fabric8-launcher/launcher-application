package io.fabric8.launcher.core.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.ImmutableBoom;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.api.projectiles.ImmutableImportFromGitProjectile;
import io.fabric8.launcher.core.api.projectiles.ImportFromGitProjectile;
import io.fabric8.launcher.core.api.projectiles.context.UploadZipProjectileContext;
import io.fabric8.launcher.core.impl.steps.GitSteps;
import io.fabric8.launcher.core.impl.steps.OpenShiftSteps;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

@Dependent
public class ImportFromGitMissionControlImpl implements MissionControl<UploadZipProjectileContext, ImportFromGitProjectile> {

    @Inject
    private StatusMessageEventBroker eventBroker;

    @Inject
    private GitSteps gitSteps;

    @Inject
    private OpenShiftSteps openShiftSteps;

    @Override
    public ImportFromGitProjectile prepare(UploadZipProjectileContext context) {
        GitRepository repository = gitSteps.findRepository(context.getGitOrganization(), context.getGitRepository());
        Path outputDir = gitSteps.clone(repository);
        try {
            Paths.unzip(context.getZipContents(), outputDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while unzipping contents", e);
        }
        return ImmutableImportFromGitProjectile.builder()
                .projectLocation(outputDir)
                .gitOrganization(context.getGitOrganization())
                .gitRepositoryName(context.getGitRepository())
                .eventConsumer(eventBroker::send)
                .openShiftProjectName(context.getProjectName())
                .build();
    }

    @Override
    public Boom launch(ImportFromGitProjectile projectile) {
        GitRepository repository = gitSteps.findRepository(projectile.getGitOrganization(), projectile.getGitRepositoryName());
        OpenShiftProject openShiftProject = openShiftSteps.createOpenShiftProject(projectile);
        openShiftSteps.configureBuildPipeline(projectile, openShiftProject, repository);
        //TODO: Push changes to Git repository
        return ImmutableBoom.builder()
                .createdProject(openShiftProject)
                .createdRepository(repository)
                .build();
    }
}