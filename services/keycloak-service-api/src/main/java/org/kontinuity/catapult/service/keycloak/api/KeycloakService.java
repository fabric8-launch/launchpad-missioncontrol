package org.kontinuity.catapult.service.keycloak.api;

/**
 * API on top of Keycloak
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface KeycloakService {
    /**
     * Returns the Openshift v3 token used for authenticating with the Openshift console
     *
     * @param token the keycloak access token
     * @return the openshift v3 token assigned to the given keycloak access token
     */
    String getOpenshiftV3Token(String token);

    /**
     * Returns the Github token
     *
     * @param token the keycloak access token
     * @return the openshift v3 token assigned to the given keycloak access token
     */
    String getGithubToken(String token);
}
