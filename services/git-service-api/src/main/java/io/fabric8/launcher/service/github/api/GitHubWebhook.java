package io.fabric8.launcher.service.github.api;

import io.fabric8.launcher.service.git.api.GitHook;

/**
 * Value object representing a webhook in GitHub
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface GitHubWebhook extends GitHook {

    /**
     * @return the events that will trigger the webhook.
     */
    GitHubWebhookEvent[] getEvents();

}
