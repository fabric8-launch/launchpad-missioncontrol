package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.kontinuity.catapult.base.identity.IdentityBuilder;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.test.GitHubTestCredentials;

/**
 * Unit Tests for the {@link GitHubService}
 * <p>
 * Relies on having environment variables set for: GITHUB_USERNAME GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class GitHubServicePojoIT extends GitHubServiceTestBase {

    @Override
    GitHubService getGitHubService() {
        return new GitHubServiceFactoryImpl().create(GitHubTestCredentials.getToken());
    }

}