package io.fabric8.launcher.service.git;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.NoSuchOrganizationException;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import static io.fabric8.launcher.service.git.GitHelper.createGitRepositoryFullName;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AbstractGitServiceIT {

    @Rule
    public final TestName testName = new TestName();

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    private List<GitRepository> repositoriesToDelete = new ArrayList<>();

    protected abstract GitServiceSpi getGitService();

    protected abstract String[] getTestHookEvents();

    protected abstract String getTestLoggedUser();

    protected abstract ImmutableGitOrganization getTestOrganization();

    protected abstract String getRawFileUrl(final String fullRepoName, final String fileName);

    @Test
    public void getOrganizationsShouldAnswerCorrectly() throws Exception {
        //This method is hard to test since we don't have the possibility to add/delete organizations

        //When: calling getOrganizations
        List<GitOrganization> organizations = getGitService().getOrganizations();

        //Then: The organizations exists and each organization is correctly constructed
        assertThat(organizations)
                .isNotNull()
                .allMatch(o -> o.getName() != null && !o.getName().isEmpty());
    }

    @Test
    public void getRepositoriesWithAnOrganization() throws Exception {
        //Given: two existing repositories belonging to the organization
        final GitRepository createdRepo1 = createRepository(getTestOrganization(), generateRepositoryName(1), "A repository belonging to an organization.");
        final GitRepository createdRepo2 = createRepository(getTestOrganization(), generateRepositoryName(2), "A second repository belonging to an organization.");
        assertThat(createdRepo1).isNotNull();
        assertThat(createdRepo2).isNotNull();

        //When: calling getRepositories with the organization
        List<GitRepository> repositories = getGitService().getRepositories(getTestOrganization());

        //Then: the two repositories are returned
        assertThat(repositories).isNotNull();
        assertThat(repositories).matches(l -> l.size() >= 2);
        assertThat(repositories).contains(createdRepo1, createdRepo2);
    }

    @Test
    public void getRepositories() throws Exception {
        //Given: two existing repositories belonging to the logged user
        final GitRepository createdRepo1 = createRepository(generateRepositoryName(1), "A repository belonging to the user.");
        final GitRepository createdRepo2 = createRepository(generateRepositoryName(2), "A second repository belonging to the user.");
        assertThat(createdRepo1).isNotNull();
        assertThat(createdRepo2).isNotNull();

        //When: calling getRepositories
        List<GitRepository> repositories = getGitService().getRepositories();

        //Then: the two repositories are returned
        assertThat(repositories).isNotNull();
        assertThat(repositories).matches(l -> l.size() >= 2);
        assertThat(repositories).contains(createdRepo1, createdRepo2);
    }

    @Test
    public void getRepositoriesWithANonexistentOrganization() throws Exception {
        assertThatExceptionOfType(NoSuchOrganizationException.class)
            .isThrownBy(() -> getGitService().getRepositories(ImmutableGitOrganization.of("nonexistent-organization")));
    }

    @Test
    public void createRepositoryWithNullName() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().createRepository(null, "desc"));
    }

    @Test
    public void createRepositoryWithEmptyName() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().createRepository("", "desc"));
    }

    @Test
    public void createRepositoryWithInvalidName() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().createRepository("owner/repo", "desc"));
    }

    @Test
    public void createRepositoryWithANonexistentOrganization() throws Exception {
        assertThatExceptionOfType(NoSuchOrganizationException.class)
                .isThrownBy(() -> getGitService().createRepository(ImmutableGitOrganization.of("nonexistent-organization"), generateRepositoryName(1), "description"));
    }

    @Test
    public void createRepositoryWithOrganization() throws Exception {
        //Given: a repository to create
        final String repositoryName = generateRepositoryName(1);

        //When: creating the repository for an organization
        final GitRepository createdRepo = createRepository(getTestOrganization(), repositoryName, "A repository belonging to an organization.");

        //Then: the created repository full name has the organization as owner
        assertThat(createdRepo)
                .isNotNull()
                .matches(r -> createGitRepositoryFullName(getTestOrganization().getName(), repositoryName).equals(r.getFullName()));

        //Then: the created repository is present in the repositories belonging to the organization
        List<GitRepository> repositories = getGitService().getRepositories(getTestOrganization());
        assertThat(repositories).isNotNull();
        assertThat(repositories).matches(l -> l.size() >= 1);
        assertThat(repositories).contains(createdRepo);
    }

    @Test
    public void createRepository() throws Exception {
        //Given: a repository to create
        final String repositoryName = generateRepositoryName(1);

        //When: creating the repository
        final GitRepository createdRepo = createRepository(repositoryName, "A logged user repository.");

        //Then: the created repository full name has the logged user as owner
        assertThat(createdRepo)
                .isNotNull()
                .matches(r -> createGitRepositoryFullName(getTestLoggedUser(), repositoryName).equals(r.getFullName()));

        //Then: the created repository is present in the repositories belonging to the logged user
        List<GitRepository> repositories = getGitService().getRepositories();
        assertThat(repositories).isNotNull();
        assertThat(repositories).matches(l -> l.size() >= 1);
        assertThat(repositories).contains(createdRepo);
    }

    @Test
    public void pushNullRepository() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().push(null, Paths.get("repoName")));
    }

    @Test
    public void pushNullPath() throws Exception {
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
        final GitRepository createdRepo = createRepository(repositoryName, "A repository with a README.md.");

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
    public void getLoggedUser() throws Exception {
        //When: getting logged user
        final GitUser loggedUser = getGitService().getLoggedUser();

        //Then: logged user is correct
        assertThat(loggedUser)
                .isNotNull()
                .matches(u -> getTestLoggedUser().equals(loggedUser.getLogin()))
                .matches(u -> u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty());
    }

    @Test
    public void getRepositoryWithNullName() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().getRepository(null));
    }

    @Test
    public void getRepositoryWithEmptyName() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().getRepository(""));
    }

    @Test
    public void getRepositoryWithInvalidName() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().getRepository("name$"));
    }

    @Test
    public void getRepositoryWithOrganizationAndInvalidName() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> getGitService().getRepository(getTestOrganization(), "'name"));
    }

    @Test
    public void getRepositoryWithANonexistentOrganization() throws Exception {
        assertThatExceptionOfType(NoSuchOrganizationException.class)
                .isThrownBy(() -> getGitService().getRepository(ImmutableGitOrganization.of("nonexistent-organization"), generateRepositoryName(1)));
    }

    @Test
    public void getRepositoryWithFullName() throws Exception {
        //Given: a freshly created organization repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(getTestOrganization(), repositoryName, "A repository belonging to an organization.");

        //When: getting repository with full name
        final Optional<GitRepository> repository = getGitService().getRepository(createGitRepositoryFullName(getTestOrganization().getName(), repositoryName));

        //Then: The repository exists and is equal to the one created.
        assertThat(repository).isPresent()
                .contains(createdRepo);
    }

    @Test
    public void getRepositoryWithName() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");

        //When: getting repository with just he name
        final Optional<GitRepository> repository = getGitService().getRepository(repositoryName);

        //Then: The repository exists and is equal to the one created.
        assertThat(repository).isPresent()
                .contains(createdRepo);
    }

    @Test
    public void getRepositoryWithNullOrganization() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().getRepository(null, "name"));
    }

    @Test
    public void getRepositoryWithOrganizationAndNullName() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().getRepository(getTestOrganization(), null));
    }

    @Test
    public void getRepositoryWithOrganization() throws Exception {
        //Given: a freshly created organization repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(getTestOrganization(), repositoryName, "A repository belonging to an organization.");

        //When: getting repository with organization and name
        final Optional<GitRepository> repository = getGitService().getRepository(getTestOrganization(), repositoryName);

        //Then: The repository exists and is equal to the one created.
        assertThat(repository).isPresent()
                .contains(createdRepo);
    }

    @Test
    public void createHookWithNullRepository() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().createHook(null, "secret", new URL("http://my-hook.com"), getTestHookEvents()));
    }

    @Test
    public void createHookWithNullWebhookUrl() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> getGitService().createHook(getTestRepository(), "secret", null, getTestHookEvents()));
    }

    @Test
    public void createHook() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
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
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
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
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
        final String webhookUrl = "http://www.openshift.org";

        //When: creating a hook with null event
        GitHook hook = getGitService().createHook(createdRepo, "1ekj\"geEUF$^ù", new URL(webhookUrl), null);

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
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
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
    public void getHooksWithoutHooks() throws Exception {
        //Given: a freshly created user repository without hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");

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
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
        GitHook hook1 = getGitService().createHook(createdRepo, "m 3K393%", new URL("http://www.redhat.com"), getTestHookEvents());
        GitHook hook2 = getGitService().createHook(createdRepo, "eafen237t", new URL("http://www.openshift.org"), getGitService().getSuggestedNewHookEvents());

        //When: get hooks on the repository
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);


        //Then: the returned hooks are the one just created
        assertThat(hooks)
                .isNotNull()
                .containsExactlyInAnyOrder(hook1, hook2);
    }

    @Test
    public void getHook() throws Exception {
        //Given: a freshly created user repository with two hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
        final URL redHatUrl = new URL("http://www.redhat.com");
        GitHook hook1 = getGitService().createHook(createdRepo, "m 3K393%", redHatUrl, getTestHookEvents());
        getGitService().createHook(createdRepo, "eafen237t", new URL("http://www.openshift.org"), getGitService().getSuggestedNewHookEvents());

        //When: get the hook for redhat url on the repository
        final Optional<GitHook> hook = getGitService().getHook(createdRepo, redHatUrl);

        //Then: the returned hooks are the one just created
        assertThat(hook)
                .isPresent()
                .contains(hook1);
    }

    @Test
    public void getHooksOnAFreshRepository() throws Exception {
        //Given: a freshly created user repository
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");

        //When: get hooks on the repository
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);


        //Then: the returned hooks is empty
        assertThat(hooks).isNotNull().isEmpty();
    }

    @Test
    public void deleteHook() throws Exception {
        //Given: a freshly created user repository with two hooks
        final String repositoryName = generateRepositoryName(1);
        final GitRepository createdRepo = createRepository(repositoryName, "A repository belonging to the user.");
        GitHook hook2 = getGitService().createHook(createdRepo, "eafen237t", new URL("http://www.openshift.org"), getGitService().getSuggestedNewHookEvents());

        //When: delete the first hook
        //getGitService().deleteWebhook(createdRepo, hook1);
        final List<GitHook> hooks = getGitService().getHooks(createdRepo);

        //Then: the returned hooks contains the second one
        assertThat(hooks)
                .isNotNull()
                .containsExactly(hook2);
    }

    private String getRawFileContent(final String fullRepoName, final String fileName) throws IOException {
        URI readmeUri = URI.create(getRawFileUrl(fullRepoName, fileName));
        final Request request = new Request.Builder()
                .url(readmeUri.toURL())
                .get()
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        final Response response = httpClient.newCall(request).execute();

        assertThat(response.code())
                .describedAs(fileName + " should have been pushed to the repo")
                .isEqualTo(200);
        return response.body().string();
    }

    private GitRepository createRepository(GitOrganization organization, String repositoryName, String description) {
        GitRepository repository = getGitService().createRepository(organization, repositoryName, description);
        repositoriesToDelete.add(repository);
        return repository;
    }

    private GitRepository createRepository(String repositoryName, String description) {
        GitRepository repository = getGitService().createRepository(repositoryName, description);
        repositoriesToDelete.add(repository);
        return repository;
    }

    private String generateRepositoryName(final int number) {
        return this.getClass().getSimpleName().toLowerCase() + "-" + testName.getMethodName().toLowerCase() + "-" + number;
    }

    @After
    public void after() {
        repositoriesToDelete.stream()
                .forEach(repo -> getGitService().deleteRepository(repo));
        repositoriesToDelete.clear();
    }
}
