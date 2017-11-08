package io.openshift.appdev.missioncontrol.service.github.api;

/**
 * Value object representing a webhook in GitHub
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface GitHubWebhook {

    /**
     * @return the name of the webhook.
     */
    String getName();

    /**
     * @return the Webhook URL
     */
    String getUrl();

    /**
     * @return the events that will trigger the webhook.
     */
    GitHubWebhookEvent[] getEvents();

}
