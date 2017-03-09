package org.kontinuity.catapult.web.api;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.kontinuity.catapult.core.api.Projectile.Type.CREATE;
import static org.kontinuity.catapult.core.api.Projectile.Type.FORK;

/**
 * Endpoint exposing the {@link org.kontinuity.catapult.core.api.Catapult} over HTTP
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(CatapultResource.PATH_CATAPULT)
@ApplicationScoped
public class CatapultResource {

   private static Logger log = Logger.getLogger(CatapultResource.class.getName());
   /*
    Paths
    */
   public static final String PATH_CATAPULT = "/catapult";
   public static final String PATH_FLING = "/fling";
   public static final String PATH_UPLOAD = "/upload";

   /*
    Catapult Query Parameters
    */
   private static final String QUERY_PARAM_SOURCE_REPO = "sourceRepo";
   private static final String QUERY_PARAM_GIT_REF = "gitRef";
   private static final String QUERY_PARAM_PIPELINE_TEMPLATE_PATH = "pipelineTemplatePath";

   private static final String QUERY_PARAM_PROJECT_LOCATION = "projectLocation";

   static final String UTF_8 = "UTF-8";
   
   @Inject
   private Catapult catapult;

   @GET
   @Path(PATH_FLING)
   public Response fling(
           @Context final HttpServletRequest request,
           @NotNull @QueryParam(QUERY_PARAM_SOURCE_REPO) final String sourceGitHubRepo,
           @NotNull @QueryParam(QUERY_PARAM_GIT_REF) final String gitRef,
           @NotNull @QueryParam(QUERY_PARAM_PIPELINE_TEMPLATE_PATH) final String pipelineTemplatePath) {

      // Construct the projectile based on input query param and the access token from the session
      final ProjectileOrResponse result = createProjectileForForkRequest(request, sourceGitHubRepo, gitRef, pipelineTemplatePath);
      if (result.hasResponse()) {
         return result.getResponse();
      }

      // Fling it
      return fling(result.getProjectile());
   }

   private ProjectileOrResponse createProjectileForCreateRequest(HttpServletRequest request, String projectLocation) {
      return createProjectileFromRequest(request, CREATE, projectLocation, null, null);
   }

   private ProjectileOrResponse createProjectileForForkRequest(HttpServletRequest request,
                                                               String sourceGitHubRepo,
                                                               String gitRef,
                                                               String pipelineTempatePath) {
      return createProjectileFromRequest(request, FORK, sourceGitHubRepo, gitRef, pipelineTempatePath);
   }

   private ProjectileOrResponse createProjectileFromRequest(HttpServletRequest request,
                                                            Projectile.Type type,
                                                            String value,
                                                            String gitRef,
                                                            String pipelineTemplatePath) {
      final String gitHubAccessToken = (String) request
              .getSession().getAttribute(GitHubResource.SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN);

      if (gitHubAccessToken == null) {
         // We've got no token yet; forward back to the GitHub OAuth service to get it
         // Define the path to hit for the GitHub OAuth
         final String gitHubOAuthPath = GitHubResource.PATH_GITHUB +
                 GitHubResource.PATH_AUTHORIZE;
         // Define the redirect to come back to (here) once OAuth is done
         final String redirectAfterOAuthPath;
         if (type == FORK) {
            redirectAfterOAuthPath = UriBuilder.fromPath(PATH_CATAPULT + PATH_FLING)
                  .queryParam(QUERY_PARAM_SOURCE_REPO, value)
                  .queryParam(QUERY_PARAM_GIT_REF, gitRef)
                  .queryParam(QUERY_PARAM_PIPELINE_TEMPLATE_PATH, pipelineTemplatePath)
                  .build().toString();
         } else {
            redirectAfterOAuthPath = UriBuilder.fromPath(PATH_CATAPULT + PATH_UPLOAD)
                  .queryParam(QUERY_PARAM_PROJECT_LOCATION, value)
                  .build().toString();
         }

         final String urlEncodedRedirectAfterOauthPath;
         try {
            urlEncodedRedirectAfterOauthPath = URLEncoder.encode(redirectAfterOAuthPath, UTF_8);
         } catch (final UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
         }
         // Create the full path
         final URI fullPath = UriBuilder.fromPath(gitHubOAuthPath)
               .queryParam(GitHubResource.QUERY_PARAM_REDIRECT_URL, urlEncodedRedirectAfterOauthPath)
               .build();
         // Forward to request an access token, noting we'll redirect back to here
         // after the OAuth process sets the token in the user session
         return new ProjectileOrResponse(Response.temporaryRedirect(fullPath).build());
      }

      if (type == FORK) {
         return new ProjectileOrResponse(ProjectileBuilder.newInstance()
               .gitHubAccessToken(gitHubAccessToken)
               .forkType()
               .sourceGitHubRepo(value)
               .gitRef(gitRef)
               .pipelineTemplatePath(pipelineTemplatePath)
               .build());
      } else {
         return new ProjectileOrResponse(ProjectileBuilder.newInstance()
               .gitHubAccessToken(gitHubAccessToken)
               .createType()
               .projectLocation(value)
               .build());
      }
   }

   private Response fling(Projectile projectile) {
      final Boom boom = catapult.fling(projectile);

      // Redirect to the console overview page
      final URI consoleOverviewUri;
      try {
         consoleOverviewUri = boom.getCreatedProject().getConsoleOverviewUrl().toURI();
         if (log.isLoggable(Level.FINEST)) {
            log.finest("Redirect issued to: " + consoleOverviewUri.toString());
         }
      } catch (final URISyntaxException urise) {
         throw new WebApplicationException("couldn't get console location do you have the environment variable set", urise);
      }
      return Response.temporaryRedirect(consoleOverviewUri).build();
   }

   @POST
   @Path(PATH_UPLOAD)
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   public Response upload(
           @Context final HttpServletRequest request,
           MultipartFormDataInput uploaded) {
      InputPart inputPart = uploaded.getFormDataMap().get("file").get(0);
      final String fileName = FileUploadHelper.getFileName(inputPart.getHeaders());

      try(InputStream inputStream = inputPart.getBody(InputStream.class,null)) {
         final java.nio.file.Path tempFile = Files.createTempDirectory(fileName);
         final File zipFileName = new File(tempFile.toFile(), fileName);
         try (FileOutputStream output = new FileOutputStream(zipFileName)) {
            IOUtils.write(IOUtils.toByteArray(inputStream), output);
            FileUploadHelper.unzip(zipFileName);

            String path = new File(tempFile.toFile(), FilenameUtils.getBaseName(fileName)).getPath();
            final ProjectileOrResponse result = createProjectileForCreateRequest(request, path);
            if (result.hasResponse()) {
               return result.getResponse();
            }

            return fling(result.getProjectile());
         }
      } catch (final IOException e) {
         throw new WebApplicationException("could not unpack zip file into temp folder", e);
      }
   }

   @GET
   @Path(PATH_UPLOAD)
   public Response uploadRedirect(@Context final HttpServletRequest request,
         @QueryParam(QUERY_PARAM_PROJECT_LOCATION) final String projectLocation) {
      // came back from GitHub oath already unpacked the zip file just fling it.
      ProjectileOrResponse result = createProjectileForCreateRequest(request, projectLocation);
      if (result.hasResponse()) {
         return result.getResponse();
      }

      return fling(result.getProjectile());
   }

   private class ProjectileOrResponse {
      private final Projectile projectile;
      private final Response response;

      ProjectileOrResponse(Projectile projectile) {
         this(projectile, null);
      }

      ProjectileOrResponse(Response response) {
         this(null, response);
      }

      private ProjectileOrResponse(Projectile projectile, Response response) {
         this.projectile = projectile;
         this.response = response;
      }

      boolean hasResponse() {
         return response != null;
      }

      Projectile getProjectile() {
         return projectile;
      }

      Response getResponse() {
         return response;
      }
   }
}
