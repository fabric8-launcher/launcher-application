package io.fabric8.launcher.service.github.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import io.fabric8.launcher.service.git.api.GitRepository;
import org.kohsuke.github.GHRepository;

/**
 * Kohsuke implementation of a {@link GitRepository}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
class KohsukeGitHubRepositoryImpl implements GitRepository {

    /**
     * Creates a new instance with the specified, required delegate
     *
     * @param repository
     */
    KohsukeGitHubRepositoryImpl(final GHRepository repository) {
        assert repository != null : "repository must be specified";
        this.delegate = repository;
    }

    private final GHRepository delegate;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullName() {
        return delegate.getFullName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getHomepage() {
        try {
            return delegate.getHtmlUrl().toURI();
        } catch (final URISyntaxException urise) {
            throw new InvalidPathException("GitHub Homepage URL can't be represented as URI", urise);
        }
    }

    @Override
    public URI getGitCloneUri() {
        try {
            return new URI(delegate.getHttpTransportUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Exception occurred while trying to get the clone URL for repo '" + delegate.getFullName() + "'", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return delegate.getDescription();
    }
}
