/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kontinuity.catapult.service.github;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubServiceFactory;
import org.kontinuity.catapult.service.github.api.GitHubRepository;

/**
 * The type Github resource.
 *
 * See the following github developer docs for the api calls used by this service.
 * https://developer.github.com/v3/oauth/
 * https://developer.github.com/v3/repos/
 * https://developer.github.com/v3/repos/forks/
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 */
@Path("/github")
@ApplicationScoped
public class GithubResource
{
   private static Logger log = Logger.getLogger(GithubResource.class.getName());
   /** The OAuth2 webflow entry point url */
   private static final String GITHUB_OAUTH_URL = "https://github.com/login/oauth/authorize";
   /** */
   private static final String FORK_ACTION = "fork";
   private static final String NEW_ACTION = "new";

   // Create a Third-party application in Github
   /** The client ID received from GitHub when the developer app was registered */
   private static String GITHUB_DEV_APP_CLIENT_ID;
   /** The client secret received from GitHub when the developer app was registered */
   private static String GITHUB_DEV_APP_SECRET;
   /** Our User-Agent HTTP header value (name of our app) */
   private static String HEADER_VALUE_USER_AGENT = "Kontinuity Catapult";

   /**
    * Initialize the GITHUB_DEV_APP_CLIENT_ID and GITHUB_DEV_APP_SECRET values from the environment by first looking
    * to the system property by the same name, will fallback to the environment variable by the same name.  If not
    * specified, fail to start this component with {@link IllegalStateException}.
    */
   @PostConstruct
   private void init() {
      // Try the system property first since this can be specified in the server configuration
      GITHUB_DEV_APP_CLIENT_ID = System.getProperty("GITHUB_DEV_APP_CLIENT_ID");
      if (GITHUB_DEV_APP_CLIENT_ID == null) {
         GITHUB_DEV_APP_CLIENT_ID = System.getenv("GITHUB_DEV_APP_CLIENT_ID");
      }
      if (GITHUB_DEV_APP_CLIENT_ID == null) {
         final String errorMessage =
                 "Failed to find binding for GITHUB_DEV_APP_CLIENT_ID as env var or sysprop; cannot init";
         log.severe(errorMessage);
         throw new IllegalStateException(errorMessage);
      }

      GITHUB_DEV_APP_SECRET = System.getProperty("GITHUB_DEV_APP_SECRET");
      if (GITHUB_DEV_APP_SECRET == null) {
         GITHUB_DEV_APP_SECRET = System.getenv("GITHUB_DEV_APP_SECRET");
      }
      if (GITHUB_DEV_APP_SECRET == null) {
         final String errorMessage =
                 "Failed to find binding for GITHUB_DEV_APP_SECRET as env var or sysprop; cannot init";
         log.severe(errorMessage);
         throw new IllegalStateException(errorMessage);
      }
   }

   /**
    * A simple verification endpoint that allows one to check if the GITHUB_DEV_APP_CLIENT_ID and GITHUB_DEV_APP_SECRET this
    * endpoint are using are as expected.
    *
    * @param clientID - the expected GITHUB_DEV_APP_CLIENT_ID value
    * @param secret - the expected GITHUB_DEV_APP_SECRET value
    * @return a json object showing the clientID and secret validity status, {clientID: "valid", secret: "invalid"}
     */
   @GET
   @Path("/verify")
   @Produces("application/json")
   public Response verifyConfiguration(@QueryParam("clientID") String clientID, @QueryParam("secret") String secret) {
      JsonObjectBuilder builder = Json.createObjectBuilder();
      if(clientID.equals(GITHUB_DEV_APP_CLIENT_ID))
         builder.add("clientID", "valid");
      else
         builder.add("clientID", "invalid");
      if(clientID.equals(GITHUB_DEV_APP_CLIENT_ID))
         builder.add("secret", "valid");
      else
         builder.add("secret", "invalid");

      return Response.ok(builder.build()).build();
   }

   /**
    * Initiate a request to fork a github repository into a user's github account.
    *
    * An example get url is:
    * http://gitfork-jbossdev.rhcloud.com/api/github/fork?repo=jboss-developer/jboss-eap-quickstarts
    * @param repo - the repository to fork, e.g., jboss-developer/jboss-eap-quickstarts
    * @return the result of the request
    * @throws Exception
    */
   @GET
   @Path("/fork")
   public Response forkRepositoryInit(@QueryParam("repo") String repo) throws Exception
   {
      log.info("Request to fork repository: "+repo);
      // Create the ghithub authorize request
      Response response = startOauthWebFlow(repo, FORK_ACTION);
      log.info("OAuth2 response: "+response.getStatusInfo());
      if(log.isLoggable(Level.FINEST)) {
         log.finest("OAuth2 response headers:");
         response.getHeaders().forEach((key, values) -> log.finest(key + ": " + values));
      }
      return response;
   }

   /**
    * Initiate a request to create a new repository in the user's github account.
    * @param repo - name name of the repository to create
    * @return the result of the request
    * @throws Exception
     */
   @GET
   @Path("/new")
   public Response newRepositoryInit(@QueryParam("repo") String repo) throws Exception
   {
      log.info("Request to create repository: "+repo);
      // Create the ghithub authorize request
      Response response = startOauthWebFlow(repo, NEW_ACTION);
      log.info("OAuth2 response: "+response.getStatusInfo());
      if(log.isLoggable(Level.FINEST)) {
         log.finest("OAuth2 response headers:");
         response.getHeaders().forEach((key, values) -> log.info(key + ": " + values));
      }
      return response;
   }

   /**
    * The callback entry point used by github during the OAuth webflow to notify us that the user has approved the
    * fork/new repository action. This is where the repository action is performed.
    *
    * @param code - a temporary code in a code parameter from github
    * @param state - the state we provided when starting the OAuth webflow
    * @return the action result reponse
    */
   @GET
   @Path("/callback")
   public Response callback(@QueryParam("code") String code, @QueryParam("state") String state)
   {
      log.info(String.format("/callback: code=%s, state=%s", code, state));
      URI uri = null;
      Client client = ClientBuilder.newClient();
      try
      {
         // Verify the state and get the repository and action to perform
         JsonObject obj = unserializeState(state);
         String repository = obj.getString("repository");
         String action = obj.getString("action");
         if(repository == null || action == null) {
            String msg = "No repository and/or action provided in callback state: " + state;
            log.warning(msg);
            return Response.serverError().entity(msg).build();
         }

         // Exchange the code for an access token
         JsonObject tokenObj = postToken(code, repository, client);
         String accessToken = tokenObj.getString("access_token");
         // Perform the action
         switch (action) {
            case NEW_ACTION:
               createRepository(accessToken, repository);
               break;
            case FORK_ACTION:
               GitHubRepository gitHubRepository = forkRepository(accessToken, repository);
               uri = gitHubRepository.getHomepage();
               break;
         }
      }
      catch (IOException e) {
         throw new WebApplicationException(e);
      }
      finally
      {
         client.close();
      }

      return Response.temporaryRedirect(uri).build();
   }

   /**
    * Convert the temporary code provided to our callback into an authorization token we can use with the github api on
    * behalf of the user.
    *
    * @param code - the temporary code in a code parameter from github provided in our callback
    * @param state - the state we provided when starting the OAuth webflow
    * @param client - the jaxrs client
    * @return the json object representing the result of the conversion request to github
    */
   private JsonObject postToken(String code, String state, Client client)
   {
      MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
      map.putSingle("client_id", GITHUB_DEV_APP_CLIENT_ID);
      map.putSingle("client_secret", GITHUB_DEV_APP_SECRET);
      map.putSingle("code", code);
      map.putSingle("state", state);
      // {"access_token":"3d4bf6b3ea93fba1dbfeeb5fa5afb5226e0cbec9","token_type":"bearer","scope":"public_repo,user:email"}
      JsonObject obj = client
              .target("https://github.com/login/oauth/access_token")
              .request()
              .accept(MediaType.APPLICATION_JSON_TYPE)
              .post(Entity.form(map), JsonObject.class);
      return obj;
   }

   private GitHubRepository forkRepository(String accessToken, String repository)
   {
      GitHubService gitHubService = GitHubServiceFactory.INSTANCE.create(accessToken);
      GitHubRepository gitHubRepository = gitHubService.fork(repository);
      return gitHubRepository;
   }

   private GitHubRepository createRepository(String accessToken, String repository) throws IOException {
      GitHubService gitHubService = GitHubServiceFactory.INSTANCE.create(accessToken);
      GitHubRepository gitHubRepository = gitHubService.create(repository, "Created via Forge Online",
              "http://forge.jboss.org", true, true, true);
      return gitHubRepository;
   }


   /**
    * Begin the github OAuth2 webflow for the given action on the repo
    * @param repo - the repository to use in the form of user/repo-name
    * @param action - the action to peform
    * @return the response to the webflow request
    */
   private Response startOauthWebFlow(String repo, String action) {
      // Create a unique state string to prevent unauthorized calls to our callback endpoint
      String state = serializeState(repo, action);

      // Create the ghithub authorize request
      Client client = ClientBuilder.newClient();
      Response response = client.target(GITHUB_OAUTH_URL)
              .queryParam("repo", repo)
              .queryParam("client_id", GITHUB_DEV_APP_CLIENT_ID)
              .queryParam("scope", "user:email,public_repo")
              .queryParam("state", state)
              .request()
              .accept(MediaType.APPLICATION_JSON_TYPE)
              .get();
      client.close();
      return response;
   }

   /**
    * Serialze the state of the request into a base64 encoded json object
    * @param repo - the name of the repository
    * @param action - the action to peform
    * @return the base64 encoded json object
    */
   private String serializeState(String repo, String action) {
      JsonObject state = Json.createObjectBuilder().add("repository", repo).add("action", action).build();
      String stateString = Base64.getEncoder().encodeToString(state.toString().getBytes());
      return stateString;
   }

   /**
    * Unserialize the request state as encoded by serializeState.
    * @param base64State - the base64 encoded json object
    * @return the unserialized json object
     */
   private JsonObject unserializeState(String base64State) {
      String json = new String(Base64.getDecoder().decode(base64State.getBytes()));
      JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
      return jsonObject;
   }
}
