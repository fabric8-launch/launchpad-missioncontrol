package org.rhd.katapult.github.api;

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
     * authentication information from the required username and required password/token
     *
     * @param githubUsername
     * @param githubToken
     * @return
     * @throws IllegalArgumentException If either the username and/or password/token is not specified
     */
    GitHubService create(String githubUsername, String githubToken) throws IllegalArgumentException;

    /**
     * Creates and returns a new instance of {@link GitHubService} initialized with
     * authentication information from the required OAuth/ token
     *
     * @param githubToken
     * @return
     * @throws IllegalArgumentException If either the token is not specified
     */
    GitHubService create(String githubToken) throws IllegalArgumentException;

}
