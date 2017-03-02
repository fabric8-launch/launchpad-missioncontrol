package org.kontinuity.catapult.core.api;

import java.io.File;

/**
 * DSL builder for creating {@link CreateProjectile} objects.  Responsible for
 * validating state before calling upon the {@link CreateProjectileBuilder#build()}
 * operation.  The following properties are required:
 * <p>
 * <ul>
 * <li>gitHubAccessToken</li>
 * <li>projectLocation</li>
 * </ul>
 * <p>
 * Each property's valid value and purpose is documented in its setter method.
 */
public class CreateProjectileBuilder extends ProjectileBuilder {
   private String projectLocation;

   CreateProjectileBuilder(String gitHubAccessToken, String openShiftProjectName) {
      super(gitHubAccessToken, openShiftProjectName);
   }

   /**
    * Creates and returns a new {@link CreateProjectile} instance based on the
    * state of this builder; if any preconditions like missing properties
    * or improper values exist, an {@link IllegalStateException} will be thrown
    *
    * @return the created {@link Projectile}
    * @throws IllegalStateException
    */
   public Projectile build() {
      super.build(this);
      ProjectileBuilder.checkSpecified("projectLocation", this.projectLocation);
      return new CreateProjectile(this);
   }

   @Override
   String createDefaultProjectName() {
      return projectLocation.substring(projectLocation.lastIndexOf(File.separator));
   }

   /**
    * Sets the projectLocation of the repository this
    * is what will be "uploaded" for the user.  Required.
    *
    * @param projectLocation
    * @return This builder
    */
   public org.kontinuity.catapult.core.api.CreateProjectileBuilder projectLocation(final String projectLocation) {
      this.projectLocation = projectLocation;
      return this;
   }

   /**
    * @return the location of the project to "upload" to GitHub.
    */
   public String getProjectLocation() {
      return projectLocation;
   }
}
