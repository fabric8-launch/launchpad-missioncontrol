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
     * Obtains the "git" protocol transport URL
     *
     * @return
     */
    String getGitTransportUrl();

    /**
     * Obtains the full repository name in form "owner/repoName"
     *
     * @return
     */
    String getFullName();

    /**
     * Obtains the github.com page for the repository
     * @return
     */
    URI getHomepage();
}
