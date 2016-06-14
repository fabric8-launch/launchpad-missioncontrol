package org.kontinuity.catapult.service.github.api;

import java.net.URI;
import java.net.URL;

/**
 * Value object representing a repository in GitHub
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface GitHubRepository {

    /**
     * @return the full repository name in form "owner/repoName"
     */
    String getFullName();

    /**
     * @return the github.com page for the repository
     */
    URI getHomepage();

	/**
	 * @param path
	 *            the path to the file
	 * @return the {@link URI} of the file with the given {@code} in the GitHub
	 *         repository
	 * @throws RuntimeException
	 *             if no file with the given {@code path} exists in the
	 *             repository
	 */
	URI getDownloadUri(String path);

	/**
	 * @return the {@link URI} to use to clone the project from GitHub
	 */
	URI getGitCloneUri();
}
