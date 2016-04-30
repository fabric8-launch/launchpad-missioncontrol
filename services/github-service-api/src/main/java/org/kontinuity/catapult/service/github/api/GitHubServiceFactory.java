package org.kontinuity.catapult.service.github.api;

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
   * @param githubToken
   * @return the created {@link GitHubService}
   * @throws IllegalArgumentException If the {@code githubToken} is not specified
   */
  GitHubService create(String githubToken);

  /**
   * Creates a new {@link GitHubService} with the specified,
   * required personal access token and the optional username
   *
   * @param githubToken
   * @param githubUsername
   * @return the created {@link GitHubService}
   * @throws IllegalArgumentException If the {@code githubToken} is not specified
   */
  // TODO: when do we need to pass an actual GitHub username ? (It's only used in tests)
  GitHubService create(String githubToken, String githubUsername);

}
