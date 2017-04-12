package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.IdentityVisitor;
import io.openshift.appdev.missioncontrol.base.identity.TokenIdentity;
import io.openshift.appdev.missioncontrol.base.identity.UserPasswordIdentity;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

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
    public GitHubService create(final Identity identity) {

        // Precondition checks
        if (identity == null) {
            throw new IllegalArgumentException("Identity is required");
        }

        final GitHub gitHub;
        try {
            // Use a cache for responses so we don't count HTTP 304 against our API quota
            final File githubCacheFolder = GitHubLocalCache.INSTANCE.getCacheFolder();
            final Cache cache = new Cache(githubCacheFolder, TENMB);
            final GitHubBuilder ghb = new GitHubBuilder()
                    .withConnector(new OkHttp3Connector(new OkHttpClient.Builder().cache(cache).build()));
            identity.accept(new IdentityVisitor() {
                @Override
                public void visit(TokenIdentity token) {
                    ghb.withOAuthToken(token.getToken());
                }

                @Override
                public void visit(UserPasswordIdentity userPassword) {
                    ghb.withPassword(userPassword.getUsername(), userPassword.getPassword());
                }
            });
            gitHub = ghb.build();
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not create GitHub client", ioe);
        }
        final GitHubService ghs = new KohsukeGitHubServiceImpl(gitHub, identity);
        log.finest(() -> "Created backing GitHub client for identity using " + identity.getClass().getSimpleName());
        return ghs;
    }

}
