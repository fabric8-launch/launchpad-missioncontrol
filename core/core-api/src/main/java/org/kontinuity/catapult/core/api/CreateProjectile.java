package org.kontinuity.catapult.core.api;

import java.nio.file.Path;

/**
 * Value object defining the inputs to {@link Catapult#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * This projectile is used to create a project in the users github.
 */
public class CreateProjectile extends Projectile {
    /**
     * Package-level access; to be invoked by {@link ProjectileBuilder}
     * and all precondition checks are its responsibility
     *
     * @param builder
     */
    CreateProjectile(CreateProjectileBuilder builder) {
        super(builder);
        this.projectLocation = builder.getProjectLocation();
        this.gitHubRepositoryDescription = builder.getGitHubRepositoryDescription();
    }

    private final Path projectLocation;
    private final String gitHubRepositoryDescription;


    public Path getProjectLocation() {
        return projectLocation;
    }

    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }
}
