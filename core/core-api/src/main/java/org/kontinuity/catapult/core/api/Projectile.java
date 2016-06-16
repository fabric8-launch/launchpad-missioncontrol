package org.kontinuity.catapult.core.api;

/**
 * Value object defining the inputs to {@link Catapult#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class Projectile {

    private final String sourceGitHubRepo;

    private final String gitHubAccessToken;

	private final String openShiftProjectName;

    /**
     * Package-level access; to be invoked by {@link ProjectileBuilder}
     * and all precondition checks are its responsibility
     */
    Projectile(final ProjectileBuilder builder){
        this.sourceGitHubRepo = builder.getSourceGitHubRepo();
        this.gitHubAccessToken = builder.getGitHubAccessToken();
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
     * @return source GitHub repository name in form "owner/repoName".
     */
    public String getSourceGitHubRepo() {
        return this.sourceGitHubRepo;
    }

	public String getOpenShiftProjectName() {
		return this.openShiftProjectName;
	}

}
