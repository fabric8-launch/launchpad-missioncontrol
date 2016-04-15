package org.kontinuity.catapult.service.openshift.api;

/**
 * Indicates a project already exists
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class DuplicateProjectException extends RuntimeException {

    private static final String MSG_PREFIX = "Project exists: ";

    private final String projectDisplayName;

    public DuplicateProjectException(final String projectDisplayName) {
        super(MSG_PREFIX + projectDisplayName);
        this.projectDisplayName = projectDisplayName;
    }
}
