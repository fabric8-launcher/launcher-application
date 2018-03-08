package io.fabric8.launcher.service.github;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.fabric8.launcher.service.git.api.GitHook;
import org.kohsuke.github.GHHook;

/**
 * {@link GitHook} implementation.
 */
public class KohsukeGitHubWebhook implements GitHook {

    /**
     * Constructor
     *
     * @param delegate the underlying {@link GHHook}
     */
    KohsukeGitHubWebhook(final GHHook delegate) {
        assert delegate != null : "delegate is required";
        this.delegate = delegate;
        this.events = delegate
                .getEvents()
                .stream()
                .map(evt -> evt.name().toLowerCase())
                .collect(Collectors.toList());
    }

    private static final String CONFIG_URL = "url";

    private final GHHook delegate;

    private final List<String> events;

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getUrl() {
        return delegate.getConfig().get(CONFIG_URL);
    }

    @Override
    public List<String> getEvents() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KohsukeGitHubWebhook that = (KohsukeGitHubWebhook) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getEvents(), that.getEvents()) &&
                Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate, events);
    }
}
