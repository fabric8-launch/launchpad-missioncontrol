package org.kontinuity.catapult.service.github.api;

import org.kontinuity.catapult.base.identity.Identity;

/**
 * A factory for the {@link GitHubService} instance.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public interface GitHubServiceFactory {

    /**
     * Creates a new {@link GitHubService} with the specified,
     * required personal access token.
     *
     * @param identity
     * @return the created {@link GitHubService}
     * @throws IllegalArgumentException If the {@code githubToken} is not specified
     */
    GitHubService create(Identity identity);
}