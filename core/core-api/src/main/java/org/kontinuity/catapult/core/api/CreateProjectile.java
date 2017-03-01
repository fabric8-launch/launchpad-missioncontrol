package org.kontinuity.catapult.core.api;

/**
 * Value object defining the inputs to {@link Catapult#fling(Projectile)};
 * immutable and pre-checked for valid state during creation.
 *
 * This projectile is used to create a project in the users github.
 */
public class CreateProjectile extends Projectile {
   private final String projectLocation;

   /**
    * Package-level access; to be invoked by {@link ProjectileBuilder}
    * and all precondition checks are its responsibility
    *
    * @param builder
    */
   CreateProjectile(ProjectileBuilder.CreateProjectileBuilder builder) {
      super(builder);
      this.projectLocation = builder.getProjectLocation();
   }

   public String getProjectLocation() {
      return projectLocation;
   }
}
