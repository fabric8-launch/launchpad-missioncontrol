package org.kontinuity.catapult.core.api;

/**
 * Value object defining the inputs to {@link Catapult#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Projectile {

    /**
     * @return the GitHub access token we have obtained from the user as part of
     * the OAuth process
     */
    String getGitHubAccessToken();

    /**
     * @return source GitHub repository name in form "owner/repoName".
     */
    String getSourceGitHubRepo();

   /**
    * @return The name to use in creating the new OpenShift project
    */
   String getOpenShiftProjectName();

   /**
    * @return The path to the pipeline template file in the repo
    */
   String getPipelineTemplatePath();

   /**
    * @return The Git reference to use
    */
   String getGitRef();
}
