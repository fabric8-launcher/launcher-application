package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.events.StatusMessageEvent;
import io.fabric8.launcher.osio.client.OsioWitClient;
import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;

import static io.fabric8.launcher.osio.steps.OsioStatusEventKind.CODEBASE_CREATED;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class WitSteps {

    @Inject
    private OsioWitClient witClient;

    public void createCodebase(OsioProjectile projectile, String stackId, GitRepository repository) {
        final String codeBaseId = witClient.createCodeBase(projectile.getSpace().getId(), stackId, repository.getGitCloneUri());
        projectile.getEventConsumer().accept(new StatusMessageEvent(projectile.getId(), CODEBASE_CREATED,
                                                                    singletonMap("codeBaseId", codeBaseId)));
    }
}
