package io.fabric8.launcher.service.git.gitea;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore("Needs to have Gitea properly setup. Used exclusively on TDD")
public class MyGiteaServiceTest {

    @Test
    public void a_should_create_repository() {
        GitService service = getGitService();
        GitRepository myrepo = service.createRepository("myrepo", "My Awesome Repository!");
        assertThat(myrepo).isNotNull();
    }

    @Test
    public void b_should_create_organization_repository() {
        GitService service = getGitService();
        GitRepository myrepo = service.createRepository(ImmutableGitOrganization.of("myorg"), "myrepo", "My Awesome Repository!");
        assertThat(myrepo).isNotNull();
    }

    @Test
    public void c_should_return_organizations() {
        GitService service = getGitService();
        List<GitOrganization> organizations = service.getOrganizations();
        assertThat(organizations).contains(ImmutableGitOrganization.of("myorg"));
    }

    @Test
    public void should_return_repository() {
        GitService service = getGitService();
        Optional<GitRepository> repository = service.getRepository("myrepo");
        GitRepository expected = ImmutableGitRepository.builder().fullName("gastaldi/myrepo")
                .homepage(URI.create("http://gitea.devtools-dev.ext.devshift.net/gastaldi/myrepo"))
                .gitCloneUri(URI.create("http://gitea.devtools-dev.ext.devshift.net/gastaldi/myrepo.git"))
                .build();
        assertThat(repository).hasValue(expected);
    }

    @Test
    public void should_return_user() {
        GitService service = getGitService();
        GitUser user = service.getLoggedUser();
        assertThat(user).isEqualTo(
                ImmutableGitUser.of("gastaldi",
                                    "https://secure.gravatar.com/avatar/fecbd47e7b167970f1650071fbacc3ba?d=identicon")
        );
    }


    @Test
    public void should_return_hooks() {
        GitService service = getGitService();
        List<GitHook> hooks = service.getHooks(ImmutableGitRepository.builder().fullName("gastaldi/myrepo")
                                                       .homepage(URI.create("unused"))
                                                       .gitCloneUri(URI.create("unused")).build());
        assertThat(hooks).hasSize(1);
    }

    @Test
    public void should_create_hook() throws Exception {
        GitService service = getGitService();
        GitRepository repository = ImmutableGitRepository.builder().fullName("gastaldi/myrepo")
                .homepage(URI.create("unused"))
                .gitCloneUri(URI.create("unused")).build();

        GitHook hook = service.createHook(repository, null, new URL("http://bar.com"), service.getSuggestedNewHookEvents());
        assertThat(hook.getUrl()).isEqualTo("http://bar.com");
        assertThat(hook.getEvents()).contains(service.getSuggestedNewHookEvents());
    }

    private GitService getGitService() {
        System.setProperty(GiteaEnvironment.LAUNCHER_BACKEND_GITEA_TOKEN.name(), "e3badab671115f81d2b85ef48011898cddfe4164");
        System.setProperty(GiteaEnvironment.LAUNCHER_BACKEND_GITEA_URL.name(), "http://gitea.devtools-dev.ext.devshift.net");
        GiteaServiceFactory factory = new GiteaServiceFactory(HttpClient.create());
        Identity identity = factory.getDefaultIdentity().orElseThrow(() -> new IllegalStateException("Default identity not found"));
        return factory.create(identity, "gastaldi");
    }


}
