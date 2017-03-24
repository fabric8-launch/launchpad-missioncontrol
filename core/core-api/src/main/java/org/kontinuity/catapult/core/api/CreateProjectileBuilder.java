package org.kontinuity.catapult.core.api;

import java.nio.file.Path;

import org.kontinuity.catapult.base.identity.Identity;

/**
 * DSL builder for creating {@link CreateProjectile} objects.  Responsible for
 * validating state before calling upon the {@link CreateProjectileBuilder#build()}
 * operation.  The following properties are required:
 * <p>
 * <ul>
 * <li>gitHubIdentity</li>
 * <li>openShiftIdentity</li>
 * <li>projectLocation</li>
 * </ul>
 * <p>
 * Each property's valid value and purpose is documented in its setter method.
 */
public class CreateProjectileBuilder extends ProjectileBuilder {
    CreateProjectileBuilder(Identity gitHubIdentity, Identity openShiftIdentity, String openShiftProjectName) {
        super(gitHubIdentity, openShiftIdentity, openShiftProjectName);
    }

    private Path projectLocation;
    /**
     * The Github Repository Description
     */
    private String gitHubRepositoryDescription = " ";

    /**
     * Creates and returns a new {@link CreateProjectile} instance based on the
     * state of this builder; if any preconditions like missing properties
     * or improper values exist, an {@link IllegalStateException} will be thrown
     *
     * @return the created {@link Projectile}
     * @throws IllegalStateException
     */
    public CreateProjectile build() {
        super.build(this);
        ProjectileBuilder.checkSpecified("projectLocation", this.projectLocation);
        ProjectileBuilder.checkSpecified("gitHubRepositoryDescription", this.gitHubRepositoryDescription);
        return new CreateProjectile(this);
    }

    /**
     * Sets the projectLocation of the repository this
     * is what will be "uploaded" for the user.  Required.
     *
     * @param projectLocation
     * @return This builder
     */
    public CreateProjectileBuilder projectLocation(final Path projectLocation) {
        this.projectLocation = projectLocation;
        return this;
    }

    /**
     * Sets the GitHub repository description when creating a new repository
     * @param description
     * @return
     */
    public CreateProjectileBuilder gitHubRepositoryDescription(final String description) {
        this.gitHubRepositoryDescription = description;
        return this;
    }

    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }

    /**
     * @return the location of the project to "upload" to GitHub.
     */
    public Path getProjectLocation() {
        return projectLocation;
    }

    @Override
    String createDefaultProjectName() {

        return projectLocation.getFileName().toString();
    }
}
