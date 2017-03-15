package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Singleton instance of a local cache of HTTP responses for conditional
 * requests against the GitHub API
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
enum GitHubLocalCache {
    INSTANCE;

    GitHubLocalCache() {
        try {
            cacheFolder = Files.createTempDirectory(PREFIX).toFile();
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not create the GitHub cache folder");
        }
    }

    private static final String PREFIX = "githubCache";

    private final File cacheFolder;

    public File getCacheFolder() {
        return cacheFolder;
    }

}
