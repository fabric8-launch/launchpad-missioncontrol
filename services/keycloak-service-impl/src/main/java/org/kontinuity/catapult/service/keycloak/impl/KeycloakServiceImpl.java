package org.kontinuity.catapult.service.keycloak.impl;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.kontinuity.catapult.base.EnvironmentSupport;
import org.kontinuity.catapult.service.keycloak.api.KeycloakService;

/**
 * The implementation of the {@link KeycloakService}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class KeycloakServiceImpl implements KeycloakService {
    private static final String TOKEN_URL_TEMPLATE = "%s/auth/realms/%s/broker/%s/token";

    private final String gitHubURL;

    private final String openshiftURL;

    private final OkHttpClient httpClient;

    @Inject
    public KeycloakServiceImpl() {
        this(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp("CATAPULT_KEYCLOAK_URL"),
             EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp("CATAPULT_KEYCLOAK_REALM"));
    }

    public KeycloakServiceImpl(String keyCloakURL, String realm) {
        this.gitHubURL = buildURL(keyCloakURL, realm, "github");
        this.openshiftURL = buildURL(keyCloakURL, realm, "openshift-v3");

        httpClient = new OkHttpClient();
    }

    /**
     * GET http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/openshift-v3/token
     * authorization: Bearer <keycloakAccessToken>
     *
     * @param keycloakAccessToken the keycloak access token
     * @return
     */
    @Override
    public String getOpenshiftV3Token(String keycloakAccessToken) {
        return getToken(openshiftURL, keycloakAccessToken);
    }

    /**
     * GET http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token
     * authorization: Bearer <keycloakAccessToken>
     *
     * @param keycloakAccessToken
     * @return
     */
    @Override
    public String getGithubToken(String keycloakAccessToken) throws IllegalArgumentException {
        return getToken(gitHubURL, keycloakAccessToken);
    }

    /**
     * GET http://sso.prod-preview.openshift.io/auth/realms/{realm}/broker/{brokerType}/token
     * authorization: Bearer <keycloakAccessToken>
     *
     * @param url
     * @param token
     * @return
     */
    private String getToken(String url, String token) {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();
        Call call = httpClient.newCall(request);
        try {
            Response response = call.execute();
            String content = response.body().string();
            // Keycloak does not respect the content-type
            if (content.startsWith("{")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(content);
                if (response.isSuccessful()) {
                    return node.get("access_token").asText();
                } else if (response.code() == 400) {
                    throw new IllegalArgumentException(node.get("errorMessage").asText());
                } else {
                    throw new IllegalStateException(node.get("errorMessage").asText());
                }
            } else {
                //access_token=1bbf10a0009d865fcb2f60d40a0ca706c7ca1e48&scope=admin%3Arepo_hook%2Cgist%2Cread%3Aorg%2Crepo%2Cuser&token_type=bearer
                String tokenParam = "access_token=";
                int idxAccessToken = content.indexOf(tokenParam);
                if (idxAccessToken < 0) {
                    throw new IllegalStateException("Access Token not found");
                }
                return content.substring(idxAccessToken + tokenParam.length(), content.indexOf('&', idxAccessToken + tokenParam.length()));
            }
        } catch (IOException io) {
            throw new IllegalStateException("Error while fetching token from keycloak", io);
        }
    }

    static String buildURL(String host, String realm, String broker) {
        return String.format(TOKEN_URL_TEMPLATE, host, realm, broker);
    }
}