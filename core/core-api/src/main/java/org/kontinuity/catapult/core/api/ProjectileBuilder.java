package org.kontinuity.catapult.core.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL builder for creating {@link Projectile} objects.  Responsible for
 * validating state before calling upon the {@link ForkProjectileBuilder#build()}
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
public class ProjectileBuilder {

   private String gitHubAccessToken;

   /**
    * the name of OpenShift project to create.
    */
   private String openShiftProjectName;


   private static final Pattern REPO_PATTERN = Pattern.compile("^[a-zA-Z_0-9\\-]+/[a-zA-Z_0-9\\-]+");

   private ProjectileBuilder() {
      // No external instances
   }

   private ProjectileBuilder(String gitHubAccessToken, String openShiftProjectName) {
      this.gitHubAccessToken = gitHubAccessToken;
      this.openShiftProjectName = openShiftProjectName;
   }

   /**
    * Creates and returns a new instance with uninitialized values
    *
    * @return a new instance of the {@link ProjectileBuilder}
    */
   public static ProjectileBuilder newInstance() {
      return new ProjectileBuilder();
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

    /*
     * Builder methods
     */

   private void build(ProjectileBuilder builder) {
      ProjectileBuilder.checkSpecified("gitHubAccessToken", this.gitHubAccessToken);
      // Default the openshiftProjectName if need be
      try {
         ProjectileBuilder.checkSpecified("openshiftProjectName", this.openShiftProjectName);
      } catch (final IllegalStateException ise) {
         this.openShiftProjectName(builder.createDefaultProjectName());
      }
   }

   String createDefaultProjectName() {
      throw new IllegalStateException("needs to be called on specific type");
   }

   /**
    * Sets the name of the OpenShift project to create. By default, the name is derived from
    * the GitHub repository to fork. Optional.
    *
    * @param openShiftProjectName
    * @return This builder
    */
   public ProjectileBuilder openShiftProjectName(final String openShiftProjectName) {
      this.openShiftProjectName = openShiftProjectName;
      return this;
   }

   /**
    * Sets the GitHub access token we have obtained from the user as part of
    * the OAuth process. Required.
    *
    * @param gitHubAccessToken
    * @return This builder
    */
   public ProjectileBuilder gitHubAccessToken(final String gitHubAccessToken) {
      this.gitHubAccessToken = gitHubAccessToken;
      return this;
   }

   /**
    * @return the GitHub access token we have obtained from the user as part of
    * the OAuth process
    */
   public String getGitHubAccessToken() {
      return this.gitHubAccessToken;
   }

   /**
    * @return The name to use in creating the new OpenShift project
    */
   public String getOpenShiftProjectName() {
      return openShiftProjectName;
   }

   public CreateProjectileBuilder createTye() {
      return new CreateProjectileBuilder(getGitHubAccessToken(), getOpenShiftProjectName());
   }

   public ForkProjectileBuilder forkType() {
      return new ForkProjectileBuilder(getGitHubAccessToken(), getOpenShiftProjectName());
   }

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
      public CreateProjectileBuilder projectLocation(final String projectLocation) {
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

   public class ForkProjectileBuilder extends ProjectileBuilder {
      private String sourceGitHubRepo;

      /**
       * the path to the file in the repo that contains the pipeline template.
       */
      private String pipelineTemplatePath;

      private String gitRef;

      ForkProjectileBuilder(String gitHubAccessToken, String openShiftProjectName) {
         super(gitHubAccessToken, openShiftProjectName);
      }

      /**
       * Creates and returns a new {@link ForkProjectile} instance based on the
       * state of this builder; if any preconditions like missing properties
       * or improper values exist, an {@link IllegalStateException} will be thrown
       *
       * @return the created {@link Projectile}
       * @throws IllegalStateException
       */
      public Projectile build() throws IllegalStateException {
         super.build(this);
         // Precondition checks
         ProjectileBuilder.checkSpecified("sourceGitHubRepo", this.sourceGitHubRepo);
         final Matcher matcher = REPO_PATTERN.matcher(sourceGitHubRepo);
         if (!matcher.matches()) {
            throw new IllegalStateException("source repo must be in form \"owner/repoName\"");
         }
         ProjectileBuilder.checkSpecified("pipelineTemplatePath", this.pipelineTemplatePath);
         ProjectileBuilder.checkSpecified("girRef", this.gitRef);

         // All good, so make a new instance
         return new ForkProjectile(this);
      }

      @Override
      String createDefaultProjectName() {
         final String sourceGitHubRepo = this.getSourceGitHubRepo();
         final String targetProjectName = this.getSourceGitHubRepo().substring(
               sourceGitHubRepo.lastIndexOf('/') + 1);

         return targetProjectName;
      }

      /**
       * Sets the source GitHub repository name in form "owner/repoName"; this
       * is what will be forked on behalf of the user.  Required.
       *
       * @param sourceGitHubRepo
       * @return This builder
       */
      public ForkProjectileBuilder sourceGitHubRepo(final String sourceGitHubRepo) {
         this.sourceGitHubRepo = sourceGitHubRepo;
         return this;
      }

      /**
       * Sets the path to file that contains the template to apply on the
       * OpenShift project. Required.
       *
       * @param pipelineTemplatePath
       * @return This builder
       */
      public ForkProjectileBuilder pipelineTemplatePath(final String pipelineTemplatePath) {
         this.pipelineTemplatePath = pipelineTemplatePath;
         return this;
      }


      /**
       * Sets Git ref to use. Required
       *
       * @param gitRef
       * @return This builder
       */
      public ForkProjectileBuilder gitRef(final String gitRef) {
         this.gitRef = gitRef;
         return this;
      }

      /**
       * @return source GitHub repository name in form "owner/repoName".
       */
      public String getSourceGitHubRepo() {
         return this.sourceGitHubRepo;
      }

      /**
       * @return the path to the file that contains the template to apply on the OpenShift project.
       */
      public String getPipelineTemplatePath() {
         return this.pipelineTemplatePath;
      }

      /**
       * @return The Git reference to use
       */
      public String getGitRef() {
         return gitRef;
      }
   }
}
