package io.fabric8.launcher.service.git;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import static io.fabric8.launcher.service.git.GitHelper.isValidGitRepositoryFullName;
import static io.fabric8.launcher.service.git.GitHelper.isValidGitRepositoryName;

public class GitHelperTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void shouldAcceptValidRepositoryNames() {
        softly.assertThat(isValidGitRepositoryName("test")).isTrue();
        softly.assertThat(isValidGitRepositoryName("Test-123_t.a")).isTrue();
        softly.assertThat(isValidGitRepositoryName("13test.")).isTrue();
    }

    @Test
    public void shouldNotAcceptInvalidRepositoryNames() {
        softly.assertThat(isValidGitRepositoryName("repo$")).isFalse();
        softly.assertThat(isValidGitRepositoryName("Test-123_t.aTest-123_t.arepo/")).isFalse();
        softly.assertThat(isValidGitRepositoryName("13ètest.")).isFalse();
        softly.assertThat(isValidGitRepositoryName("test^")).isFalse();
    }

    @Test
    public void shouldAcceptValidRepositoryFullNames() {
        softly.assertThat(isValidGitRepositoryFullName("owner/repo")).isTrue();
        softly.assertThat(isValidGitRepositoryFullName("Test-123_t.a/Test-123_t.arepo")).isTrue();
        softly.assertThat(isValidGitRepositoryFullName("13test./aa")).isTrue();
    }

    @Test
    public void shouldNotAcceptInvalidRepositoryFullNames() {
        softly.assertThat(isValidGitRepositoryFullName("owner/repo$")).isFalse();
        softly.assertThat(isValidGitRepositoryFullName("Test-123_t.aTest-123_t.arepo")).isFalse();
        softly.assertThat(isValidGitRepositoryFullName("13test./")).isFalse();
        softly.assertThat(isValidGitRepositoryFullName("test")).isFalse();
    }
}