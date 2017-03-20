package org.kontinuity.catapult.service.keycloak.impl;

import org.kontinuity.catapult.base.EnvironmentSupport;
import org.kontinuity.catapult.base.identity.Identity;
import org.kontinuity.catapult.base.identity.IdentityFactory;
import org.kontinuity.catapult.service.keycloak.api.KeycloakService;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class KeycloakServiceMock implements KeycloakService {

    @Override
    public Identity getOpenShiftIdentity(String token) {
        // Read from the ENV variables
        String user = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp("CATAPULT_OPENSHIFT_USERNAME");
        String password = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp("CATAPULT_OPENSHIFT_PASSWORD");
        return IdentityFactory.createFromUserPassword(user,password);
    }

    @Override
    public Identity getGithubIdentity(String token) {
        // Try using the provided Github token
        String val = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp("GITHUB_TOKEN");
        return IdentityFactory.createFromToken(val);
    }

    public static boolean isEnabled() {
        String user = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("CATAPULT_OPENSHIFT_USERNAME");
        String password = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("CATAPULT_OPENSHIFT_PASSWORD");
        return (user != null && password != null);
    }

}
