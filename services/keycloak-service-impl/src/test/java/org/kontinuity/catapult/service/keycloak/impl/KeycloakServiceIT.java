package org.kontinuity.catapult.service.keycloak.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class KeycloakServiceIT {
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenOpenshift() {
        String token = "token";
        KeycloakServiceImpl service = new KeycloakServiceImpl();
        Assert.assertNotNull(service.getOpenShiftIdentity(token));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenGithub() {
        String token = "InvalidToken";
        KeycloakServiceImpl service = new KeycloakServiceImpl();
        Assert.assertNotNull(service.getGithubIdentity(token));
    }
}