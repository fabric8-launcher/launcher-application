package io.fabric8.launcher.core.impl;

import java.io.File;

import javax.inject.Inject;

import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.projectiles.ImportFromGitProjectile;
import io.fabric8.launcher.core.api.projectiles.context.ImmutableUploadZipProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.UploadZipProjectileContext;
import io.fabric8.launcher.core.spi.DirectoryReaper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ImportFromGitMissionControlIT {
    /**
     * @return a war file containing all the required classes and dependencies
     * to test the {@link MissionControl}
     */
    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.createDeployment()
                .addAsResource(new File("src/test/resources/extra-files.zip"), "extra-files.zip")
                .addAsWebInfResource(new File("src/main/resources/META-INF/beans.xml"), "beans.xml")
                .addClass(MockServiceProducers.class);
    }

    @Inject
    MissionControl<UploadZipProjectileContext, ImportFromGitProjectile> missionControl;

    @Inject
    DirectoryReaper directoryReaper;

    @Test
    public void mission_control_must_be_injected() {
        assertThat(missionControl).isNotNull();
    }

    @Test
    public void prepare_should_clone_repository() {
        UploadZipProjectileContext context = ImmutableUploadZipProjectileContext.builder()
                .projectName("foo")
                .zipContents(getClass().getClassLoader().getResourceAsStream("extra-files.zip"))
                .gitOrganization("fabric8-launcher")
                .gitRepository("launcher-planning")
                .build();
        ImportFromGitProjectile projectile = missionControl.prepare(context);
        try {
            assertThat(projectile.getProjectLocation().resolve("README.adoc")).exists();
            assertThat(projectile)
                    .hasFieldOrPropertyWithValue("gitOrganization", context.getGitOrganization())
                    .hasFieldOrPropertyWithValue("gitRepositoryName", context.getGitRepository())
                    .hasFieldOrPropertyWithValue("openShiftProjectName", context.getProjectName());
        } finally {
            directoryReaper.delete(projectile.getProjectLocation());
        }
    }

}
