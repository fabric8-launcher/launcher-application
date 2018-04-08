package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.osio.client.OsioWitClient;
import io.fabric8.launcher.service.git.api.GitRepository;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class WitSteps {

    @Inject
    private OsioWitClient witClient;

    public void createCodebase(String spaceId, String stackId, GitRepository repository) {
        witClient.createCodeBase(spaceId, stackId, repository.getGitCloneUri());
    }
}
