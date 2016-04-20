package org.kontinuity.catapult.service.github.api;

import java.util.ServiceLoader;

/**
 * Defines a {@link ServiceLoader} implementation that returns instances of
 * {@link GitHubService}.  Implementations must supply a no-arg constructor.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface GitHubServiceLoader {

    /**
     * Creates and returns a new instance of {@link GitHubService} initialized with
     * authentication information from the required password/personal access token
     * and optional username.
     *
     * Note that for a token, the username is not required.  For password, the username is.
     *
     * @param githubToken
     * @param githubUsername
     * @return the created {@link GitHubService}
     * @throws IllegalArgumentException If the password/token is not specified
     */
    GitHubService create(String githubToken, String githubUsername) throws IllegalArgumentException;

}
