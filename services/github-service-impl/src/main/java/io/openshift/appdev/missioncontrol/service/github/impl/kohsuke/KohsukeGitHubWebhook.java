package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

import java.util.stream.Collectors;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhookEvent;
import org.kohsuke.github.GHHook;

/**
 * {@link GitHubWebhook} implementation.
 */
public class KohsukeGitHubWebhook implements GitHubWebhook {

    /**
     * Constructor
     *
     * @param delegate the underlying {@link GHHook}
     */
    public KohsukeGitHubWebhook(final GHHook delegate) {
        assert delegate != null : "delegate is required";
        this.delegate = delegate;
        this.events = delegate
                .getEvents()
                .stream()
                .map(evt -> GitHubWebhookEvent.valueOf(evt.name()))
                .collect(Collectors.toList())
                .toArray(new GitHubWebhookEvent[delegate.getEvents().size()]);
    }

    private static final String CONFIG_URL = "url";

    private final GHHook delegate;

    private final GitHubWebhookEvent[] events;

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getUrl() {
        return delegate.getConfig().get(CONFIG_URL);
    }

    @Override
    public GitHubWebhookEvent[] getEvents() {
        return events;
    }

}
