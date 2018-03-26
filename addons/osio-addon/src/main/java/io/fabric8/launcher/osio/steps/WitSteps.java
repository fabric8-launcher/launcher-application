package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.osio.client.OsioApiClient;
import io.fabric8.launcher.service.git.api.GitRepository;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class WitSteps {

    @Inject
    private OsioApiClient osioApiClient;

    public void createCodebase(String spaceId, String stackId, GitRepository repository) {
        osioApiClient.createCodeBase(spaceId, stackId, repository.getGitCloneUri());
    }
}
