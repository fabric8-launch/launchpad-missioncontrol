package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.OkHttpConnector;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubServiceFactory;

/**
 * Implementation of the {@link GitHubServiceFactory}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@ApplicationScoped
public class GitHubServiceFactoryImpl implements GitHubServiceFactory {

    private static final int TENMB = 10 * 1024 * 1024; // 10MB

    private Logger log = Logger.getLogger(GitHubServiceFactoryImpl.class.getName());

    @Override
    public GitHubService create(final String githubToken) {
        return create(githubToken, null);
    }

    // TODO: when do we need to pass an actual GitHub username ? (It's only used in tests)
    @Override
    public GitHubService create(final String githubToken, final String githubUsername) {

        // Precondition checks
        if (githubToken == null || githubToken.isEmpty()) {
            throw new IllegalArgumentException("password/token is required");
        }

        final GitHub gitHub;
        try {
            // Use a cache for responses so we don't count HTTP 304 against our API quota
            final File githubCacheFolder = GitHubLocalCache.INSTANCE.getCacheFolder();
            final Cache cache = new Cache(githubCacheFolder, TENMB);
            final GitHubBuilder ghb = new GitHubBuilder()
                    .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))));
            if (githubUsername == null) {
                ghb.withOAuthToken(githubToken);
            } else {
                ghb.withOAuthToken(githubToken, githubUsername);
            }
            gitHub = ghb.build();
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not create GitHub client", ioe);
        }
        final GitHubService ghs = new KohsukeGitHubServiceImpl(gitHub);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created backing GitHub client for user " + githubUsername);
        }
        return ghs;
    }

}
