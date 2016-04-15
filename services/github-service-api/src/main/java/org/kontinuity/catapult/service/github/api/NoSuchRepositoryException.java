package org.kontinuity.catapult.service.github.api;

/**
 * Indicates a specified repository does not exist
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class NoSuchRepositoryException extends RuntimeException {

    private static final String MSG_PREFIX = "No such repository named ";

    public NoSuchRepositoryException(final String repositoryName) {
        super(MSG_PREFIX + repositoryName);
    }
}
