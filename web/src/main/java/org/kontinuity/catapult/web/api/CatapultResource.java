package org.kontinuity.catapult.web.api;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

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

   /*
    Catapult Query Parameters
    */
   private static final String QUERY_PARAM_SOURCE_REPO = "sourceRepo";
   private static final String QUERY_PARAM_GIT_REF = "gitRef";
   private static final String QUERY_PARAM_PIPELINE_TEMPLATE_PATH = "pipelineTemplatePath";

   static final String UTF_8 = "UTF-8";
   
   @Inject
   private Catapult catapult;

   @Inject
   private ProjectileBuilder projectileBuilder;

   @GET
   @Path(PATH_FLING)
   public Response fling(
           @Context final HttpServletRequest request,
           @NotNull @QueryParam(QUERY_PARAM_SOURCE_REPO) final String sourceGitHubRepo,
           @NotNull @QueryParam(QUERY_PARAM_GIT_REF) final String gitRef,
           @NotNull @QueryParam(QUERY_PARAM_PIPELINE_TEMPLATE_PATH) final String pipelineTemplatePath) {

      // First let's see if we have a GitHub access token stored in the session
      final String gitHubAccessToken = (String) request
              .getSession().getAttribute(GitHubResource.SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN);
      if (gitHubAccessToken == null) {
         // We've got no token yet; forward back to the GitHub OAuth service to get it
         final URI gitHubOAuthUri;
         try {
            // Define the path to hit for the GitHub OAuth
            final String gitHubOAuthPath = GitHubResource.PATH_GITHUB +
                    GitHubResource.PATH_AUTHORIZE;
            // Define the redirect to come back to (here) once OAuth is done
            final String redirectAfterOAuthPath = PATH_CATAPULT +
                    PATH_FLING +
                    '?' +
                    CatapultResource.QUERY_PARAM_SOURCE_REPO +
                    '=' + sourceGitHubRepo + '&' +
                    CatapultResource.QUERY_PARAM_GIT_REF +
                    '=' + gitRef + '&' +
                    CatapultResource.QUERY_PARAM_PIPELINE_TEMPLATE_PATH +
                    '=' + pipelineTemplatePath;
            final String urlEncodedRedirectAfterOauthPath;
            try {
               urlEncodedRedirectAfterOauthPath = URLEncoder.encode(redirectAfterOAuthPath, UTF_8);
            } catch (final UnsupportedEncodingException uee) {
               throw new RuntimeException(uee);
            }
            // Create the full path
            final String fullPath = new StringBuilder().
                    append(gitHubOAuthPath).
                    append('?').
                    append(GitHubResource.QUERY_PARAM_REDIRECT_URL).
                    append('=').
                    append(urlEncodedRedirectAfterOauthPath).toString();
            gitHubOAuthUri = new URI(fullPath);
         } catch (final URISyntaxException urise) {
            return Response.serverError().entity(urise).build();
         }
         // Forward to request an access token, noting we'll redirect back to here
         // after the OAuth process sets the token in the user session
         return Response.temporaryRedirect(gitHubOAuthUri).build();
      }
      // Construct the projectile based on input query param and the access token from the session
      final Projectile projectile = projectileBuilder
              .sourceGitHubRepo(sourceGitHubRepo)
              .gitHubAccessToken(gitHubAccessToken)
              .gitRef(gitRef)
              .pipelineTemplatePath(pipelineTemplatePath)
              .build();

      // Fling it
      final Boom boom = catapult.fling(projectile);

      // Redirect to the console overview page
      final URI consoleOverviewUri;
      try {
         consoleOverviewUri = boom.getCreatedProject().getConsoleOverviewUrl().toURI();
         if (log.isLoggable(Level.FINEST)) {
            log.finest("Redirect issued to: " + consoleOverviewUri.toString());
         }
      } catch (final URISyntaxException urise) {
         return Response.serverError().entity(urise).build();
      }
      return Response.temporaryRedirect(consoleOverviewUri).build();
   }
}
