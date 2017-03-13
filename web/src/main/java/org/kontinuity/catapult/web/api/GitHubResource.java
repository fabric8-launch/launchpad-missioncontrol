package org.kontinuity.catapult.web.api;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.kontinuity.catapult.service.github.CatapultAppId;
import org.kontinuity.catapult.service.github.CatapultAppOAuthSecret;

/**
 * Endpoint to request and obtain a GitHub OAuth token
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(GitHubResource.PATH_GITHUB)
@ApplicationScoped
public class GitHubResource {

    /**
     * Message if no state is found in the user session when invoking the callback
     */
    public static final String MSG_NO_STATE = "No initiating state found in the session; potential in-the-middle attack";

    public static final String MSG_UNMATCHED_STATE = "Expected state does not match specified; potential in-the-middle attack";

    public static final String MSG_NO_REDIRECT = "No redirect found in the state after OAuth";

    /*
     Paths
     */
    public static final String PATH_GITHUB = "/github";

    public static final String PATH_CALLBACK = "/callback";

    public static final String PATH_AUTHORIZE = "/authorize";

    public static final String PATH_VERIFY = "/verify";

    /*
     Session Attribute Names
     */
    static final String SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN = "GitHubAccessToken";

    static final String SESSION_ATTRIBUTE_GITHUB_OAUTH_STATE_UUID = "GitHubOAuthState";

    /*
     Our query parameters
     */
    static final String QUERY_PARAM_REDIRECT_URL = "redirect_url";

    /**
     * The OAuth2 webflow entry point url
     */
    private static final String GITHUB_OAUTH_AUTH_URL = "https://github.com/login/oauth/authorize";

    private static final String GITHUB_OAUTH_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";

    /*
     GitHub API Query Parameters
     */
    private static final String QUERY_PARAM_CLIENT_ID_NAME = "client_id";

    private static final String QUERY_PARAM_SCOPE_NAME = "scope";

    private static final String QUERY_PARAM_SCOPE_VALUE = "user:email,public_repo";

    private static final String QUERY_PARAM_STATE_NAME = "state";

    private static final String QUERY_PARAM_CODE_NAME = "code";

    private static final String JSON_PARAM_TOKEN_NAME = "access_token";

    /*
     GitHub Token Request Object Parameters
     */
    private static final String TOKEN_REQUEST_PARAM_CLIENT_ID_NAME = "client_id";

    private static final String TOKEN_REQUEST_PARAM_CLIENT_SECRET_NAME = "client_secret";

    private static final String TOKEN_REQUEST_PARAM_CODE_NAME = "code";

    private static final String TOKEN_REQUEST_PARAM_STATE_NAME = "state";

    /*
     Our state attributes
     */
    private static final String STATE_ATTR_UUID = "uuid";

    private static final String STATE_ATTR_REDIRECT_URL = "redirect_url";

    /*
     Our verification attributes
     */
    private static final String VERIFY_ATTR_ACCESS_TOKEN = "session_has_github_access_token";

    private static Logger log = Logger.getLogger(GitHubResource.class.getName());

    @Inject
    @CatapultAppId
    private String catapultAppId;

    /**
     * Id of the Catapult application OAuth secret on GitHub.
     */
    @Inject
    @CatapultAppOAuthSecret
    private String catapultOAuthDevAppSecret;

    /**
     * Returns a JSON object with a single attribute,
     * {@link GitHubResource#VERIFY_ATTR_ACCESS_TOKEN}, with boolean value
     * indicating whether the current user session has a GitHub access token
     * associated
     *
     * @param request The incoming request
     * @return
     */
    @GET
    @Path(GitHubResource.PATH_VERIFY)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verify(
            @Context final HttpServletRequest request) {
        final boolean hasTokenInSession = request.getSession().
                getAttribute(SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN) != null;
        final JsonObject json = Json.createObjectBuilder().
                add(VERIFY_ATTR_ACCESS_TOKEN, hasTokenInSession).
                build();
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Starts the GitHub Webflow OAuth process as documented by
     * https://developer.github.com/v3/oauth/#web-application-flow
     *
     * @param request The {@link HttpServletRequest associated with this call}
     * @return
     */
    @GET
    @Path(GitHubResource.PATH_AUTHORIZE)
    public Response authorize(
            @Context final HttpServletRequest request,
            @NotNull @QueryParam(GitHubResource.QUERY_PARAM_REDIRECT_URL) final String redirectUrl) {
        // Create a unique state ID to track so we prevent in-the-middle attacks
        final String uuid = UUID.randomUUID().toString();
        request.getSession().setAttribute(SESSION_ATTRIBUTE_GITHUB_OAUTH_STATE_UUID, uuid);

        // Create the state object to send to GitHub
        // (will be returned to us in the OAuth callback)
        final String state = GitHubResource.serializeState(uuid, redirectUrl);

        // Create the GitHub authorize request
        URI target = UriBuilder.fromUri(GITHUB_OAUTH_AUTH_URL)
                .queryParam(QUERY_PARAM_CLIENT_ID_NAME, catapultAppId)
                .queryParam(QUERY_PARAM_SCOPE_NAME, QUERY_PARAM_SCOPE_VALUE)
                .queryParam(QUERY_PARAM_STATE_NAME, state)
                .queryParam(QUERY_PARAM_REDIRECT_URL, redirectUrl)
                .build();
        return Response.temporaryRedirect(target).build();
    }

    /**
     * The callback entry point used by github during the OAuth webflow; here we convert
     * the specified code into a token and store that token in the user session
     * under the key {@link GitHubResource#SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN}
     *
     * @param code  - a temporary code in a code parameter from github
     * @param state - the state we provided when starting the OAuth webflow
     * @return the action result response
     */
    @GET
    @Path(GitHubResource.PATH_CALLBACK)
    public Response callback(
            @Context final HttpServletRequest request,
            @NotNull @QueryParam(QUERY_PARAM_CODE_NAME) final String code,
            @NotNull @QueryParam(QUERY_PARAM_STATE_NAME) final String state) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("%s: code=%s, state=%s", GitHubResource.PATH_CALLBACK, code, state));
        }

        final HttpSession session = request.getSession();

        // Deserialize the state
        final JsonObject stateJson = GitHubResource.deserializeState(state);
        final String uuid = stateJson.getString(STATE_ATTR_UUID);

        // Check our UUID matches
        final String expectedUuid = (String) session.getAttribute(SESSION_ATTRIBUTE_GITHUB_OAUTH_STATE_UUID);
        if (expectedUuid == null) {
            return Response.status(Response.Status.UNAUTHORIZED).
                    entity(MSG_NO_STATE).build();
        }
        if (!expectedUuid.equals(uuid)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    MSG_UNMATCHED_STATE).build();
        } else {
            // Clear the UUID state
            session.removeAttribute(SESSION_ATTRIBUTE_GITHUB_OAUTH_STATE_UUID);
        }

        // Exchange the code for an access token
        final JsonObject tokenObj = postToken(code, state);
        final String accessToken = tokenObj.getString(JSON_PARAM_TOKEN_NAME);

        // Put the access token in the user session so we may obtain it later
        session.setAttribute(SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN, accessToken);

        // Redirect to where we said we should go
        final String redirectUrl = stateJson.getString(STATE_ATTR_REDIRECT_URL);
        final String unencodedRedirectUrl;
        try {
            unencodedRedirectUrl = URLDecoder.decode(redirectUrl, CatapultResource.UTF_8);
        } catch (final UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
        log.warning("Redirect URL: " + unencodedRedirectUrl);
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            return Response.serverError().entity(new IllegalStateException(MSG_NO_REDIRECT)).build();
        }
        final URI redirectUri = UriBuilder.fromUri(redirectUrl).build();
        return Response.temporaryRedirect(redirectUri).build();
    }

    /**
     * Serialize the state of the request into a base64 encoded json object
     *
     * @param uuid        - The unique ID of this state
     * @param redirectUrl - The URL to redirect to when we're done
     * @return the base64 encoded json object
     */
    private static String serializeState(final String uuid, final String redirectUrl) {
        assert uuid != null && !uuid.isEmpty() : "uuid is required";
        assert redirectUrl != null && !redirectUrl.isEmpty() : "redirectUrl is required";
        final JsonObject state = Json.createObjectBuilder().
                add(STATE_ATTR_UUID, uuid).
                add(STATE_ATTR_REDIRECT_URL, redirectUrl).
                build();
        return Base64.getEncoder().encodeToString(state.toString().getBytes());
    }

    /**
     * Deserialize the request state as encoded by serializeState.
     *
     * @param base64State - the base64 encoded json object
     * @return the deserialized json object
     */
    private static JsonObject deserializeState(final String base64State) {
        assert base64State != null && !base64State.isEmpty() : "base64state is required";
        final String json = new String(Base64.getDecoder().decode(base64State.getBytes()));
        try (final JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }

    /**
     * Convert the temporary code provided to our callback into an authorization token we can use with the github api on
     * behalf of the user.
     *
     * @param code  - the temporary code in a code parameter from github provided in our callback
     * @param state - the state we provided when starting the OAuth webflow
     * @return the json object representing the result of the conversion request to github
     */
    private JsonObject postToken(final String code, final String state) {
        final Client client = ClientBuilder.newClient();

        // Put request parameters in a Map
        final MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        map.putSingle(TOKEN_REQUEST_PARAM_CLIENT_ID_NAME, this.catapultAppId);
        map.putSingle(TOKEN_REQUEST_PARAM_CLIENT_SECRET_NAME, this.catapultOAuthDevAppSecret);
        map.putSingle(TOKEN_REQUEST_PARAM_CODE_NAME, code);
        map.putSingle(TOKEN_REQUEST_PARAM_STATE_NAME, state);

        // Make the POST request
        final JsonObject obj = client
                .target(GITHUB_OAUTH_ACCESS_TOKEN_URL)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.form(map), JsonObject.class);

        // Return the response as a JSON object
        return obj;
    }
}
