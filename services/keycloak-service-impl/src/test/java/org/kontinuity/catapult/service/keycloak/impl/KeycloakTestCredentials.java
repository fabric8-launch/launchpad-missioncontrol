package org.kontinuity.catapult.service.keycloak.impl;

import org.kontinuity.catapult.base.EnvironmentSupport;

/**
 * Used to obtain the GitHub credentials from the environment
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class KeycloakTestCredentials {

    private KeycloakTestCredentials() {
        // No instances
    }

    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN = "CATAPULT_KEYCLOAK_TOKEN";

    /**
     * @return the GitHub token
     */
    public static String getToken() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN);
    }
}
