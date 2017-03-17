package org.kontinuity.catapult.core.api;

/**
 * Value object defining the inputs to {@link Catapult#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public abstract class Projectile {

    private final String gitHubAccessToken;

    private final String openShiftAccessToken;

    private final String openShiftProjectName;

    /**
     * Package-level access; to be invoked by {@link ProjectileBuilder}
     * and all precondition checks are its responsibility
     */
    Projectile(final ProjectileBuilder builder) {
        this.gitHubAccessToken = builder.getGitHubAccessToken();
        this.openShiftAccessToken = builder.getOpenshiftAccessToken();
        this.openShiftProjectName = builder.getOpenShiftProjectName();
    }

    /**
     * @return the GitHub access token we have obtained from the user as part of
     * the OAuth process
     */
    public String getGitHubAccessToken() {
        return this.gitHubAccessToken;
    }

    /**
     * @return the Openshift access token we have obtained from the user as part of
     * the OAuth process
     */
    public String getOpenShiftAccessToken() {
        return openShiftAccessToken;
    }

    /**
     * @return The name to use in creating the new OpenShift project
     */
    public String getOpenShiftProjectName() {
        return openShiftProjectName;
    }
}