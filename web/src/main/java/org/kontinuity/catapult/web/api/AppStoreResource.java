package org.kontinuity.catapult.web.api;

import org.kontinuity.catapult.base.EnvironmentSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Endpoint to request the appstore
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(AppStoreResource.PATH_APPSTORE)
@ApplicationScoped
public class AppStoreResource {

    /*
     Paths
    */
    public static final String PATH_APPSTORE = "/appstore";
    public static final String PATH_URL = "/applications";
    private static final String DEFAULT_APPSTORE_URL = "http://rawgit.com/redhat-kontinuity/appstore/master/appstore.json";
    private static final String NAME_SYSPROP_CATAPULT_APP_STORE_URL = "CATAPULT_APPSTORE_URL";

    /**
     * Provides a list of applications in the appstore in JSON format
     *
     * @return
     */
    @GET
    @Path(PATH_URL)
    @Produces(MediaType.APPLICATION_JSON)
    public Response applications() {
        JsonObject result = getApplicationJson();
        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }

    private JsonObject getApplicationJson() {
        // App store URL
        String appStoreUrlString = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(NAME_SYSPROP_CATAPULT_APP_STORE_URL, DEFAULT_APPSTORE_URL);

        // JsonObjectBuilder builder = Json.createObjectBuilder();
        Client client = ClientBuilder.newClient();
        try {
            JsonObject result = client.target(appStoreUrlString)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get(JsonObject.class);
            return result;
        } finally {
            client.close();
        }
    }
}