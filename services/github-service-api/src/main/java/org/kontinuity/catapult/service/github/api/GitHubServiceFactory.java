package org.kontinuity.catapult.service.github.api;

import java.util.ServiceLoader;

/**
 * Creates {@link GitHubService} instances using the {@link ServiceLoader} pattern to decouple
 * the API from the implementing provider.  Requires a {@link ServiceLoader} implementation class with
 * no-arg constructor to be defined in "META-INF/services" on the classpath
 * <p>
 * https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public enum GitHubServiceFactory {

    INSTANCE;

    private static final String MSG_MORE_THAN_ONE_IMPL = "Cannot have more than one implementation of " +
            GitHubServiceLoader.class.getName() + " defined as a " +
            ServiceLoader.class.getName() + " implementation";

    private static final String MSG_NEEDS_IMPL = "Must have one implementation of " +
            GitHubServiceLoader.class.getName() + " defined as a " +
            ServiceLoader.class.getName() + ".";

    private final ServiceLoader<GitHubServiceLoader> serviceLoader = ServiceLoader.load(GitHubServiceLoader.class);


    /**
     * Creates a new {@link GitHubService} with the specified,
     * required username and password/personal access token
     *
     * @param githubToken
     * @param githubUsername
     * @return
     * @throws IllegalArgumentException If either the username and/or password/token is not specified
     */
    public GitHubService create(final String githubToken, final String githubUsername) throws IllegalArgumentException {

        // Precondition checks
        if (githubToken == null || githubToken.isEmpty()) {
            throw new IllegalArgumentException("password/token is required");
        }

        // We only want one implementation
        GitHubServiceLoader ghsl = null;
        for (final GitHubServiceLoader candidate : serviceLoader) {
            if (ghsl != null) {
                throw new IllegalStateException(MSG_MORE_THAN_ONE_IMPL);
            }
            ghsl = candidate;
        }
        if (ghsl == null) {
            throw new IllegalStateException(MSG_NEEDS_IMPL);
        }

        // Create
        final GitHubService gsh = ghsl.create(githubToken, githubUsername);
        return gsh;
    }

    /**
     * Creates a new {@link GitHubService} with the specified,
     * required OAuth token
     *
     * @param githubToken
     * @return
     * @throws IllegalArgumentException If the OAuth token is not specified
     */
    public GitHubService create(final String githubToken) throws IllegalArgumentException {
        return this.create(githubToken, null);
    }
}
