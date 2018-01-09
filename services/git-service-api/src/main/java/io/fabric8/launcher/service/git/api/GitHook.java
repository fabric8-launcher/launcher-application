package io.fabric8.launcher.service.git.api;

/**
 * Value object representing a webhook in Git
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitHook {

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
    GitHookEvent[] getEvents();
}
