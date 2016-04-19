package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.kohsuke.github.GHRepository;
import org.kontinuity.catapult.service.github.api.GitHubRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Kohsuke implementation of a {@link GitHubRepository}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
class KohsukeGitHubRepositoryImpl implements GitHubRepository {

    private Logger log = Logger.getLogger(KohsukeGitHubRepositoryImpl.class.getName());

    private final GHRepository delegate;

    /**
     * Creates a new instance with the specified, required delegate
     *
     * @param repository
     */
    KohsukeGitHubRepositoryImpl(final GHRepository repository) {
        assert repository != null : "repository must be specified";
        this.delegate = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGitTransportUrl() {
        return delegate.getGitTransportUrl();
    }

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
            throw new RuntimeException("GitHub Homepage URL can't be represented as URI", urise);
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
