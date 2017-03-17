package org.kontinuity.catapult.service.keycloak.impl;

import org.junit.Assert;
import org.junit.Test;

public class KeycloakServiceTest {
    @Test
    public void testBuildUrl() {
        String url = KeycloakServiceImpl.buildURL("http://sso.prod-preview.openshift.io", "fabric8", "github");
        Assert.assertEquals("http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }
}
