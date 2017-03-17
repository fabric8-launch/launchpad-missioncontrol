package org.kontinuity.catapult.service.keycloak.impl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * To get a valid token:
 *
 * - Open Chrome and go to: http://prod-preview.openshift.io/
 * - After authentication, grab the token from the URL. There should be a http://prod-preview.openshift.io/?token=XXX (Use the network console if needed)
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class KeycloakServiceIT {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenOpenshift() {
        String token = "token";
        KeycloakServiceImpl service = new KeycloakServiceImpl();
        Assert.assertNotNull(service.getOpenshiftV3Token(token));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenGithub() {
        String token = "InvalidToken";
        KeycloakServiceImpl service = new KeycloakServiceImpl();
        Assert.assertNotNull(service.getGithubToken(token));
    }

    @Test
    public void testValidTokenGithub() {
        String token = KeycloakTestCredentials.getToken();
        KeycloakServiceImpl service = new KeycloakServiceImpl();
        Assert.assertNotNull(service.getGithubToken(token));
    }

    @Test
    public void testValidTokenOpenshift() {
        String token = KeycloakTestCredentials.getToken();
        KeycloakServiceImpl service = new KeycloakServiceImpl();
        Assert.assertNotNull(service.getOpenshiftV3Token(token));
    }

}
