package io.fabric8.launcher.service.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitRepositoryFilter;
import io.fabric8.launcher.service.git.api.NoSuchOrganizationException;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import okhttp3.Request;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import static io.fabric8.launcher.service.git.Gits.createGitRepositoryFullName;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AbstractGitServiceTest {

    private static final String DEFAULT_DESCRIPTION = "The 'best' test repository description with special chars $^¨`\".";

    @Rule
    public final TestName testName = new TestName();

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private List<String> repositoriesToDelete = new ArrayList<>();

    protected abstract GitServiceSpi getGitService();

    protected abstract String[] getTestHookEvents();

    protected abstract String getTestLoggedUser();

    protected abstract ImmutableGitOrganization getTestOrganization();

    protected abstract String getRawFileUrl(final String fullRepoName, final String fileName);

    @Test
    public void getOrganizationsShouldAnswerCorrectly() {
        //This method is hard to test since we don't have the possibility to add/delete organizations

        //When: calling getOrganizations
        List<GitOrganization> organizations = getGitService().getOrganizations();

        //Then: The organizations exists and each organization is correctly constructed
        assertThat(organizations)
                .isNotNull()
                .allMatch(o -> o.getName() != null && !o.getName().isEmpty())
                .isSorted();
    }


    @Test
    public void getRepositoriesWithANonexistentOrganization() {
        final ImmutableGitRepositoryFilter filter = ImmutableGitRepositoryFilter.builder().withOrganization(ImmutableGitOrganization.of("nonexistent-organization")).build();
        assertThatExceptionOfType(NoSuchOrganizationException.class)
                .isThrownBy(() -> getGitService().getRepositories(filter));
    }

    @Test
    public void searchRepositoriesWithNullFilter() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> getGitService().getRepositories(null));
    }

    @Test
    public void getRepositories() {
        //Given: three existing repositories belonging to the logged user and three to the test organization
        final GitRepository userHat1Repo = createRepository(generateRepositoryName("hat1"));
        final GitRepository userHat2Repo = createRepository(generateRepositoryName("hat2"));
        final GitRepository userCapRepo = createRepository(generateRepositoryName("cap"));
        final GitRepository orgHat1Repo = createRepository(getTestOrganization(), generateRepositoryName("hat1"));
        final GitRepository orgHat2Repo = createRepository(getTestOrganization(), generateRepositoryName("hat2"));
        final GitRepository orgCapRepo = createRepository(getTestOrganization(), generateRepositoryName("cap"));

        assertThat(userHat1Repo).isNotNull();
        assertThat(userHat2Repo).isNotNull();
        assertThat(userCapRepo).isNotNull();
        assertThat(orgHat1Repo).isNotNull();
        assertThat(orgHat2Repo).isNotNull();
        assertThat(orgCapRepo).isNotNull();

        //When: calling getRepositories with an empty filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.of()))
                //Then
                .as("the result contains all the logged user repositories")
                .isNotNull()
                .contains(userHat1Repo, userHat2Repo, userCapRepo)
                .doesNotContain(orgHat1Repo, orgHat2Repo, orgCapRepo);

        //When: calling getRepositories with a name containing "hat1" as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withNameContaining("hat1").build()))
                //Then
                .as("the result contains the user hat1 repository")
                .isNotNull()
                .containsExactly(userHat1Repo);

        //When: calling getRepositories with a name containing "hat" as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withNameContaining("hat").build()))
                //Then
                .as("the result contains all the logged user repositories containing 'hat'")
                .isNotNull()
                .containsExactlyInAnyOrder(userHat1Repo, userHat2Repo);

        //When: calling getRepositories with a name containing "no-match" as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withNameContaining("no-match").build()))
                //Then
                .as("there is no result containing 'no-match'")
                .isNotNull()
                .isEmpty();

        //When: calling getRepositories with the test organization as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withOrganization(getTestOrganization()).build()))
                //Then
                .as("the result contains all the test organization repositories")
                .isNotNull()
                .contains(orgHat1Repo, orgHat2Repo, orgCapRepo)
                .doesNotContain(userHat1Repo, userHat2Repo, userCapRepo);

        //When: calling getRepositories  with the test organization and with a name containing "hat1" as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withOrganization(getTestOrganization()).withNameContaining("hat1").build()))
                //Then
                .as("the result contains the userRep1 repository")
                .isNotNull()
                .containsExactlyInAnyOrder(orgHat1Repo);

        //When: calling getRepositories  with the test organization and with a name containing "hat" as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withOrganization(getTestOrganization()).withNameContaining("hat").build()))
                //Then
                .as("the result contains all the logged user repositories containing 'hat'")
                .isNotNull()
                .containsExactlyInAnyOrder(orgHat1Repo, orgHat2Repo);

        //When: calling getRepositories  with the test organization and with a name containing "no-match" as filter
        softly.assertThat(getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withOrganization(getTestOrganization()).withNameContaining("no-match").build()))
                //Then
                .as("there is no result containing 'no-match'")
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void createRepositoryWithNullName() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().createRepository(null, "desc"));
    }

    @Test
    public void createRepositoryWithEmptyName() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().createRepository("", "desc"));
    }

    @Test
    public void createRepositoryWithInvalidName() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().createRepository("owner/repo", "desc"));
    }

    @Test
    public void createRepositoryWithANonexistentOrganization() {
        assertThatExceptionOfType(NoSuchOrganizationException.class)
                .isThrownBy(() -> getGitService().createRepository(ImmutableGitOrganization.of("nonexistent-organization"), generateRepositoryName(1), "description"));
    }

    @Test
    public void createRepositoryWithOrganization() {
        //Given: a repository to create
        final String repositoryName = generateRepositoryName(1);

        //When: creating the repository for an organization
        final GitRepository createdRepo = createRepository(getTestOrganization(), repositoryName);

        //Then: the created repository full name has the organization as owner
        assertThat(createdRepo)
                .isNotNull()
                .matches(r -> createGitRepositoryFullName(getTestOrganization().getName(), repositoryName).equals(r.getFullName()));

        //Then: the created repository is present in the repositories belonging to the organization
        List<GitRepository> repositories = getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withOrganization(getTestOrganization()).build());
        assertThat(repositories).isNotNull();
        assertThat(repositories).matches(l -> l.size() >= 1);
        assertThat(repositories).contains(createdRepo);
    }

    @Test
    public void createRepository() throws Exception {
        //Given: a repository to create
        final String repositoryName = generateRepositoryName(1);

        //When: creating the repository
        final GitRepository createdRepo = createRepository(repositoryName);

        //Then: the created repository full name has the logged user as owner
        assertThat(createdRepo)
                .isNotNull()
                .matches(r -> createGitRepositoryFullName(getTestLoggedUser(), repositoryName).equals(r.getFullName()));

        //Then: the created repository is present in the repositories belonging to the logged user
        Optional<GitRepository> repo = getGitService().getRepository(repositoryName);
        assertThat(repo)
                .isNotNull()
                .isPresent()
                .contains(createdRepo);
    }

    @Test
    public void pushNullRepository() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().push(null, Paths.get("repoName")));
    }

    @Test
    public void pushNullPath() {
        final ImmutableGitRepository repository = getTestRepository();
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().push(repository, null));
    }

    private ImmutableGitRepository getTestRepository() {
        return ImmutableGitRepository.builder()
                .fullName("ia3andy/nonexistent-repo")
                .gitCloneUri(URI.create("http://fakeclone.com"))
                .homepage(URI.create("http://fakehome.com"))
                .build();
    }

    @Test
    public void pushFile() throws Exception {
        //Given: a freshly created repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);

        //When: pushing a README.md file
        final Path tempDirectory = tmpFolder.getRoot().toPath();
        final String readmeFileName = "README.md";
        final Path file = tmpFolder.newFile(readmeFileName).toPath();
        final String readmeContent = "Read me to know more";
        Files.write(file, singletonList(readmeContent), Charset.forName("UTF-8"));
        getGitService().push(createdRepo, tempDirectory);

        //Then: The raw README.md file content is correct
        assertThat(getRawFileContent(createdRepo.getFullName(), readmeFileName))
                .isEqualTo(readmeContent + "\n");
    }

    @Test
    public void getLoggedUser() {
        //When: getting logged user
        final GitUser loggedUser = getGitService().getLoggedUser();

        //Then: logged user is correct
        assertThat(loggedUser)
                .isNotNull()
                .matches(u -> getTestLoggedUser().equals(loggedUser.getLogin()))
                .matches(u -> u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty());
    }

    @Test
    public void getRepositoryWithNullName() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().getRepository(null));
    }

    @Test
    public void getRepositoryWithEmptyName() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().getRepository(""));
    }

    @Test
    public void getRepositoryWithInvalidName() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().getRepository("name$"));
    }

    @Test
    public void getRepositoryWithOrganizationAndInvalidName() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().getRepository(getTestOrganization(), "'name"));
    }

    @Test
    public void getRepositoryWithANonexistentOrganization() {
        assertThatExceptionOfType(NoSuchOrganizationException.class)
                .isThrownBy(() -> getGitService().getRepository(ImmutableGitOrganization.of("nonexistent-organization"), generateRepositoryName(1)));
    }

    @Test
    public void getRepositoryWithFullName() {
        //Given: a freshly created organization repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(getTestOrganization(), repositoryName);

        //When: getting repository with full name
        final Optional<GitRepository> repository = getGitService().getRepository(createGitRepositoryFullName(getTestOrganization().getName(), repositoryName));

        //Then: The repository exists and is equal to the one created.
        assertThat(repository).isPresent()
                .contains(createdRepo);
    }

    @Test
    public void getRepositoryWithName() {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);

        //When: getting repository with just he name
        final Optional<GitRepository> repository = getGitService().getRepository(repositoryName);

        //Then: The repository exists and is equal to the one created.
        assertThat(repository).isPresent()
                .contains(createdRepo);
    }

    @Test
    public void getRepositoryWithNullOrganization() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().getRepository(null, "name"));
    }

    @Test
    public void getRepositoryWithOrganizationAndNullName() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().getRepository(getTestOrganization(), null));
    }

    @Test
    public void getRepositoryWithOrganization() {
        //Given: a freshly created organization repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(getTestOrganization(), repositoryName);

        //When: getting repository with organization and name
        final Optional<GitRepository> repository = getGitService().getRepository(getTestOrganization(), repositoryName);

        //Then: The repository exists and is equal to the one created.
        assertThat(repository).isPresent()
                .contains(createdRepo);
    }

    @Test
    public void createHookWithNullRepository() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().createHook(null, "secret", new URL("http://my-hook.com"), getTestHookEvents()));
    }

    @Test
    public void createHookWithNullWebhookUrl() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().createHook(getTestRepository(), "secret", null, getTestHookEvents()));
    }

    @Test
    public void createHook() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        final String webhookUrl = "http://www.openshift.org";

        //When: creating a hook
        GitHook hook = getGitService().createHook(createdRepo, "1ekj\"geEUF$^ù", new URL(webhookUrl), getTestHookEvents());

        //Then: the created hook is valid
        assertThat(hook).isNotNull();
        assertThat(hook.getName()).isNotEmpty();
        assertThat(hook.getUrl()).isEqualTo(webhookUrl);
        assertThat(hook.getEvents()).containsExactlyInAnyOrder(getTestHookEvents());
    }

    @Test
    public void createHookWithoutEvents() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        final String webhookUrl = "http://www.openshift.org";

        //When: creating a hook without event
        GitHook hook = getGitService().createHook(createdRepo, "1ekj\"geEUF$^ù", new URL(webhookUrl));

        //Then: the created hook use suggested new hook events
        assertThat(hook).isNotNull();
        assertThat(hook.getName()).isNotEmpty();
        assertThat(hook.getUrl()).isEqualTo(webhookUrl);
        assertThat(hook.getEvents()).containsExactlyInAnyOrder(getGitService().getSuggestedNewHookEvents());
    }

    @Test
    public void createHookWithNullEvents() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        final String webhookUrl = "http://www.openshift.org";

        //When: creating a hook with null event
        GitHook hook = getGitService().createHook(createdRepo, "1ekj\"geEUF$^ù", new URL(webhookUrl), (String[]) null);

        //Then: the created hook use suggested new hook events
        assertThat(hook).isNotNull();
        assertThat(hook.getName()).isNotEmpty();
        assertThat(hook.getUrl()).isEqualTo(webhookUrl);
        assertThat(hook.getEvents()).containsExactlyInAnyOrder(getGitService().getSuggestedNewHookEvents());
    }

    @Test
    public void createHookWithoutSecret() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        final String webhookUrl = "https://www.redhat.com";

        //When: creating a hook without secret
        GitHook hook = getGitService().createHook(createdRepo, null, new URL(webhookUrl), getTestHookEvents());

        //Then: the created hook is valid
        assertThat(hook).isNotNull();
        assertThat(hook.getName()).isNotEmpty();
        assertThat(hook.getUrl()).isEqualTo(webhookUrl);
        assertThat(hook.getEvents()).containsExactlyInAnyOrder(getTestHookEvents());
    }

    @Test
    public void getHooksWithoutHooks() {
        //Given: a freshly created user repository without hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);

        //When: get hooks on the repository
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);

        //Then: the returned hooks is empty
        assertThat(hooks)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void getHooks() throws Exception {
        //Given: a freshly created user repository with two hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        GitHook hook1 = getGitService().createHook(createdRepo, "m 3K393%", new URL("http://www.redhat.com"), getTestHookEvents());
        GitHook hook2 = getGitService().createHook(createdRepo, "eafen237t", new URL("http://www.openshift.org"), getGitService().getSuggestedNewHookEvents());

        //When: get hooks on the repository
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);


        //Then: the returned hooks are the ones just created
        assertThat(hooks)
                .isNotNull()
                .containsExactlyInAnyOrder(hook1, hook2);
    }

    @Test
    public void getHook() throws Exception {
        //Given: a freshly created user repository with two hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        final URL redHatUrl = new URL("http://www.redhat.com");
        GitHook hook1 = getGitService().createHook(createdRepo, "m 3K393%", redHatUrl, getTestHookEvents());
        getGitService().createHook(createdRepo, "eafen237t", new URL("http://www.openshift.org"), getGitService().getSuggestedNewHookEvents());

        //When: get the hook for redhat url on the repository
        final Optional<GitHook> hook = getGitService().getHook(createdRepo, redHatUrl);

        //Then: the returned hooks is the one just created
        assertThat(hook)
                .isPresent()
                .contains(hook1);
    }

    @Test
    public void getHooksOnAFreshRepository() {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);

        //When: get hooks on the repository
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);


        //Then: the returned hooks is empty
        assertThat(hooks).isNotNull().isEmpty();
    }

    @Test
    public void deleteHook() throws Exception {
        //Given: a freshly created user repository with two hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);
        GitHook hook1 = getGitService().createHook(createdRepo, "eafen237t", new URL("http://www.openshift.org"), getGitService().getSuggestedNewHookEvents());
        GitHook hook2 = getGitService().createHook(createdRepo, "m 3K393%", new URL("http://www.redhat.com"), getTestHookEvents());

        //When: delete the second hook
        getGitService().deleteWebhook(createdRepo, hook2);
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);

        //Then: the returned hooks contains the first one
        assertThat(hooks)
                .isNotNull()
                .containsExactly(hook1);
    }


    @Test
    public void cloneRepository() throws Exception {
        // Just run this if outside simulation mode
        Assume.assumeFalse("This test should only run if HoverFly is not in simulation mode",
                           LauncherHoverflyRuleConfigurer.isHoverflyInSimulationMode());
        //Given: a freshly created repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName);

        //When: pushing a README.md file
        final Path tempDirectory = tmpFolder.newFolder("repository-to-clone").toPath();
        final String readmeFileName = "README.md";
        final Path file = tempDirectory.resolve(readmeFileName);
        final String readmeContent = "Read me to know more";
        Files.write(file, singletonList(readmeContent), Charset.forName("UTF-8"));
        getGitService().push(createdRepo, tempDirectory);


        Path clonedRepository = tmpFolder.newFolder("cloned-repository").toPath();
        getGitService().clone(createdRepo, clonedRepository);


        //Then: README.md exists and the raw README.md file content is correct
        assertThat(clonedRepository.resolve(readmeFileName))
                .exists()
                .hasContent(readmeContent);
    }


    private String getRawFileContent(final String fullRepoName, final String fileName) throws IOException {
        URI readmeUri = URI.create(getRawFileUrl(fullRepoName, fileName));
        final Request request = new Request.Builder()
                .url(readmeUri.toURL())
                .get()
                .build();
        return HttpClient.create().executeAndMap(request, response -> {
            assertThat(response.code())
                    .describedAs(fileName + " should have been pushed to the repo")
                    .isEqualTo(200);
            try {
                return response.body().string();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private GitRepository createRepository(GitOrganization organization, String repositoryName) {
        repositoriesToDelete.add(createGitRepositoryFullName(organization.getName(), repositoryName));
        GitRepository repository = getGitService().createRepository(organization, repositoryName, DEFAULT_DESCRIPTION);
        return repository;
    }

    protected GitRepository createRepository(String repositoryName) {
        repositoriesToDelete.add(createGitRepositoryFullName(getTestLoggedUser(), repositoryName));
        GitRepository repository = getGitService().createRepository(repositoryName, DEFAULT_DESCRIPTION);
        return repository;
    }

    protected String generateRepositoryName(final int number) {
        return generateRepositoryName(String.valueOf(number));
    }

    protected String generateRepositoryName(final String suffix) {
        final String name = testName.getMethodName().toLowerCase();
        return "it-" + name.substring(0, Math.min(name.length(), 40)) + "-" + suffix;
    }

    /**
     * Use it when there is an error and repositories are not deleted by the test (it's quicker than cleaning manually).
     */
    @Test
    @Ignore
    public void cleanRepositories() {
        //Clean own repository
        getGitService().getRepositories(ImmutableGitRepositoryFilter.of())
                .stream().filter(r -> r.getFullName().startsWith(createGitRepositoryFullName(getTestLoggedUser(), "it-")))
                .forEach(r -> getGitService().deleteRepository(r.getFullName()));
        //Clean organization repository
        getGitService().getRepositories(ImmutableGitRepositoryFilter.builder().withOrganization(getTestOrganization()).build())
                .stream().filter(r -> r.getFullName().startsWith(createGitRepositoryFullName(getTestOrganization().getName(), "it-")))
                .forEach(r -> getGitService().deleteRepository(r.getFullName()));
    }

    @After
    public void after() {
        repositoriesToDelete.stream()
                .forEach(repo -> getGitService().deleteRepository(repo));
        repositoriesToDelete.clear();
    }
}
