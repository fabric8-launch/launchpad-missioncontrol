package org.kontinuity.catapult.core.impl;

import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL builder for creating {@link Projectile} objects.  Responsible for
 * validating state before calling upon the {@link ProjectileBuilderImpl#build()}
 * operation.  The following properties are required:
 * <p>
 * <ul>
 * <li>sourceGitHubRepo</li>
 * <li>gitHubAccessToken</li>
 * </ul>
 * <p>
 * Each property's valid value and purpose is documented in its setter method.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class ProjectileBuilderImpl implements Projectile, ProjectileBuilder {

    private String sourceGitHubRepo;

    private String gitHubAccessToken;

    /**
     * the name of OpenShift project to create.
     */
    private String openShiftProjectName;

    /**
     * the path to the file in the repo that contains the pipeline template.
     */
    private String pipelineTemplatePath;

    private String gitRef;

    private static final Pattern REPO_PATTERN = Pattern.compile("^[a-zA-Z_0-9\\-]+/[a-zA-Z_0-9\\-]+");


    /**
     * Creates and returns a new {@link Projectile} instance based on the
     * state of this builder; if any preconditions like missing properties
     * or improper values exist, an {@link IllegalStateException} will be thrown
     *
     * @return the created {@link Projectile}
     * @throws IllegalStateException
     */
    @Override
    public Projectile build() throws IllegalStateException {
        // Precondition checks
        ProjectileBuilderImpl.checkSpecified("sourceGitHubRepo", this.sourceGitHubRepo);
        final Matcher matcher = REPO_PATTERN.matcher(sourceGitHubRepo);
        if (!matcher.matches()) {
            throw new IllegalStateException("source repo must be in form \"owner/repoName\"");
        }
        ProjectileBuilderImpl.checkSpecified("gitHubAccessToken", this.gitHubAccessToken);
        ProjectileBuilderImpl.checkSpecified("pipelineTemplatePath", this.pipelineTemplatePath);
        ProjectileBuilderImpl.checkSpecified("girRef", this.gitRef);

        // Default the openshiftProjectName if need be
        try {
            ProjectileBuilderImpl.checkSpecified("openshiftProjectName", this.openShiftProjectName);
        } catch (final IllegalStateException ise) {
            final String sourceGitHubRepo = this.getSourceGitHubRepo();
            final String targetProjectName = this.getSourceGitHubRepo().substring(
                    sourceGitHubRepo.lastIndexOf('/') + 1);
            this.openShiftProjectName(targetProjectName);
        }

        return this;
    }

    /**
     * Ensures the specified value is not null or empty, else throws
     * an {@link IllegalArgumentException} citing the specified name
     * (which is also required ;) )
     *
     * @param value
     * @throws IllegalStateException
     */
    private static void checkSpecified(final String name,
                                       final String value) throws IllegalStateException {
        assert name != null && !name.isEmpty() : "name is required";
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(name + " must be specified");
        }
    }

    /**
     * Builder methods
     */

    /**
     * Sets the source GitHub repository name in form "owner/repoName"; this
     * is what will be forked on behalf of the user.  Required.
     *
     * @param sourceGitHubRepo
     * @return This builder
     */
    public ProjectileBuilderImpl sourceGitHubRepo(final String sourceGitHubRepo) {
        this.sourceGitHubRepo = sourceGitHubRepo;
        return this;
    }

    /**
     * Sets the GitHub access token we have obtained from the user as part of
     * the OAuth process. Required.
     *
     * @param gitHubAccessToken
     * @return This builder
     */
    public ProjectileBuilderImpl gitHubAccessToken(final String gitHubAccessToken) {
        this.gitHubAccessToken = gitHubAccessToken;
        return this;
    }

    /**
     * Sets the path to file that contains the template to apply on the
     * OpenShift project. Required.
     *
     * @param pipelineTemplatePath
     * @return This builder
     */
    public ProjectileBuilderImpl pipelineTemplatePath(final String pipelineTemplatePath) {
        this.pipelineTemplatePath = pipelineTemplatePath;
        return this;
    }

    /**
     * Sets the name of the OpenShift project to create. By default, the name is derived from
     * the GitHub repository to fork. Optional.
     *
     * @param openShiftProjectName
     * @return This builder
     */
    public ProjectileBuilderImpl openShiftProjectName(final String openShiftProjectName) {
        this.openShiftProjectName = openShiftProjectName;
        return this;
    }

    /**
     * Sets Git ref to use. Required
     *
     * @param gitRef
     * @return This builder
     */
    @Override
    public ProjectileBuilderImpl gitRef(final String gitRef) {
        this.gitRef = gitRef;
        return this;
    }

    /**
     * @return source GitHub repository name in form "owner/repoName".
     */
    @Override
    public String getSourceGitHubRepo() {
        return this.sourceGitHubRepo;
    }

    /**
     * @return the GitHub access token we have obtained from the user as part of
     * the OAuth process
     */
    @Override
    public String getGitHubAccessToken() {
        return this.gitHubAccessToken;
    }

    /**
     * @return The name to use in creating the new OpenShift project
     */
    @Override
    public String getOpenShiftProjectName() {
        return openShiftProjectName;
    }

    /**
     * @return the path to the file that contains the template to apply on the OpenShift project.
     */
    @Override
    public String getPipelineTemplatePath() {
        return this.pipelineTemplatePath;
    }

    /**
     * @return The Git reference to use
     */
    @Override
    public String getGitRef() {
        return gitRef;
    }
}
