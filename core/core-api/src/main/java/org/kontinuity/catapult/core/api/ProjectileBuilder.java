package org.kontinuity.catapult.core.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL builder for creating {@link Projectile} objects.  Responsible for
 * validating state before calling upon the {@link ProjectileBuilder#build()}
 * operation.  The following properties are required:
 *
 * <ul>
 *     <li>sourceGitHubRepo</li>
 *     <li>gitHubAccessToken</li>
 * </ul>
 *
 * Each property's valid value and purpose is documented in its setter method.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface ProjectileBuilder {

    /**
     * Creates and returns a new {@link Projectile} instance based on the
     * state of this builder; if any preconditions like missing properties
     * or improper values exist, an {@link IllegalStateException} will be thrown
     *
     * @return the created {@link Projectile}
     * @throws IllegalStateException
     */
    Projectile build() throws IllegalStateException;

    /**
     * Builder methods
     */

    /**
     * Sets the source GitHub repository name in form "owner/repoName"; this
     * is what will be forked on behalf of the user.  Required.
     * @param sourceGitHubRepo
     * @return This builder
     */
    ProjectileBuilder sourceGitHubRepo(final String sourceGitHubRepo);
    
    /**
     * Sets the GitHub access token we have obtained from the user as part of
     * the OAuth process. Required.
     *
     * @param gitHubAccessToken
     * @return This builder
     */
    ProjectileBuilder gitHubAccessToken(final String gitHubAccessToken);

   /**
    * Sets the path to file that contains the template to apply on the
    * OpenShift project. Required.
    *
    * @param pipelineTemplatePath
    * @return This builder
    */
   ProjectileBuilder pipelineTemplatePath(final String pipelineTemplatePath);

   /**
    * Sets the name of the OpenShift project to create. By default, the name is derived from
    * the GitHub repository to fork. Optional.
    * @param openShiftProjectName
    * @return This builder
    */
   ProjectileBuilder openShiftProjectName(final String openShiftProjectName);

   /**
    * Sets Git ref to use. Required
    *
    * @param gitRef
    * @return This builder
    */
   ProjectileBuilder gitRef(final String gitRef);
}
