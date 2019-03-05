package io.fabric8.launcher.osio.enrichers;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.osio.client.ImmutableSpace;
import io.fabric8.launcher.osio.projectiles.ImmutableOsioLaunchProjectile;
import io.fabric8.launcher.osio.projectiles.OsioLaunchProjectile;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitUserGoEnricherTest {

    @Test
    public void should_replace_imports() throws Exception {
        Path sourceDir = Paths.get(getClass().getResource(getClass().getSimpleName()).toURI());
        // Mock a RhoarBooster
        RhoarBooster booster = mock(RhoarBooster.class);
        when(booster.getGitRepo()).thenReturn("https://github.com/golang-starters/golang-rest-http");

        GitService gitService = mock(GitService.class);
        when(gitService.getLoggedUser()).thenReturn(ImmutableGitUser.of("myself", null));

        OsioLaunchProjectile projectile = ImmutableOsioLaunchProjectile.builder()
                .booster(booster)
                .projectLocation(sourceDir)
                .gitRepositoryName("example")
                .pipelineId("foo")
                .openShiftProjectName("bar")
                .space(ImmutableSpace.builder().id("spaceId").name("spaceName").build())
                .build();

        GitUserGoEnricher enricher = new GitUserGoEnricher(gitService);
        enricher.accept(projectile);
        assertThat(sourceDir.resolve("example.go")).hasSameContentAs(sourceDir.resolve("example.go.expected"));
    }
}