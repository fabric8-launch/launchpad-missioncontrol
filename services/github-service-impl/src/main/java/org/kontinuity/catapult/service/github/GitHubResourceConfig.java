package org.kontinuity.catapult.service.github;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.kontinuity.catapult.base.EnvironmentSupport;

/**
 * CDI Bean producer to allow for injection of configuration settings
 */
@ApplicationScoped
public class GitHubResourceConfig {

    private static Logger log = Logger.getLogger(GitHubResourceConfig.class.getName());

    /**
     * Name of the environment variable or system property for the GitHub OAuth
     * Client ID
     */
    private static String ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_ID = "CATAPULT_GITHUB_APP_CLIENT_ID";

    /**
     * Name of the environment variable or system property for the GitHub OAuth
     * Client Secret
     */
    private static String ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_SECRET = "CATAPULT_GITHUB_APP_CLIENT_SECRET";

    @Produces
    @CatapultAppId
    public String getCatapultApplicationId() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_ID);
    }

    @Produces
    @CatapultAppOAuthSecret
    public String getCatapultApplicationOAuthSecret() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_SECRET);
    }
}
