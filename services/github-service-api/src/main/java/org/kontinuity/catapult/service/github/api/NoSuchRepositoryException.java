package org.kontinuity.catapult.service.github.api;

import java.text.MessageFormat;

/**
 * Indicates a specified repository does not exist.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class NoSuchRepositoryException extends RuntimeException {

    /** version number of this serializable class. */
    private static final long serialVersionUID = -78123136358123472L;
    
    /** The message pattern. */
    private static final String MSG_PATTERN = "No such repository named {0}";

    /**
     * Constructor
     * @param repositoryName name of the GitHub repository
     */
    public NoSuchRepositoryException(final String repositoryName) {
        super(MessageFormat.format(MSG_PATTERN, repositoryName));
    }
}
