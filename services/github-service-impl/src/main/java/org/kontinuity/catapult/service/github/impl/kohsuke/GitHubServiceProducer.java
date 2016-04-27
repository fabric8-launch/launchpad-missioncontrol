/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.OkHttpConnector;
import org.kontinuity.catapult.service.github.api.GitHubService;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

/**
 * CDI Producer for the {@link GitHubService} instance.
 * 
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@ApplicationScoped
public class GitHubServiceProducer {

    private Logger log = Logger.getLogger(KohsukeGitHubRepositoryImpl.class.getName());

	private static final int TENMB = 10 * 1024 * 1024; // 10MB

	/**
     * Creates a new {@link GitHubService} with the specified,
     * required username and password/personal access token
     *
     * @param githubToken
     * @param githubUsername
     * @return the created {@link GitHubService}
     * @throws IllegalArgumentException If either the {@code githubToken} and/or {@code githubUsername} is not specified
     */
	@Produces
    public GitHubService create(@GitHubToken final String githubToken, @GitHubUsername final String githubUsername) {

        // Precondition checks
        if (githubToken == null || githubToken.isEmpty()) {
            throw new IllegalArgumentException("password/token is required");
        }

        final GitHub gitHub;
        try {
            // Use a cache for responses so we don't count HTTP 304 against our API quota
            final File githubCacheFolder = GitHubLocalCache.INSTANCE.getCacheFolder();
            final Cache cache = new Cache(githubCacheFolder, TENMB);
            GitHubBuilder ghb = new GitHubBuilder()
                    .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))));
            if(githubUsername == null) {
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
	
	/**
	 * @return the GitHub username from the system properties or environment variables.
	 * @throws IllegalArgumentException if the config element was not found.
	 */
	@Produces
	@GitHubUsername
	public String getGitHubUsername() {
		return GitHubCredentials.getUsername();
	}

	/**
	 * @return the GitHub token from the system properties or environment variables.
	 * @throws IllegalArgumentException if the config element was not found.
	 */
	@Produces
	@GitHubToken
	public String getGitHubToken() {
		return GitHubCredentials.getToken();
	}
	
}
