package org.kontinuity.catapult.core.api;

import org.kontinuity.catapult.base.identity.Identity;

/**
 * Value object defining the inputs to {@link Catapult#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public abstract class Projectile {

    private final Identity gitHubIdentity;

    private final Identity openShiftIdentity;

    private final String openShiftProjectName;

    /**
     * Package-level access; to be invoked by {@link ProjectileBuilder}
     * and all precondition checks are its responsibility
     */
    Projectile(final ProjectileBuilder builder) {
        this.gitHubIdentity = builder.getGitHubIdentity();
        this.openShiftIdentity = builder.getOpenShiftIdentity();
        this.openShiftProjectName = builder.getOpenShiftProjectName();
    }

    /**
     * @return the GitHub identity
     */
    public Identity getGitHubIdentity() {
        return this.gitHubIdentity;
    }

    /**
     * @return the Openshift identity
     */
    public Identity getOpenShiftIdentity() {
        return openShiftIdentity;
    }

    /**
     * @return The name to use in creating the new OpenShift project
     */
    public String getOpenShiftProjectName() {
        return openShiftProjectName;
    }
}