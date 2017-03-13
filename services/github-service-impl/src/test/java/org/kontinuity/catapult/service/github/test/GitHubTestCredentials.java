package org.kontinuity.catapult.service.github.test;

import org.kontinuity.catapult.base.EnvironmentSupport;

/**
 * Used to obtain the GitHub credentials from the environment
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubTestCredentials {

    private GitHubTestCredentials() {
        // No instances
    }

    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_USERNAME = "GITHUB_USERNAME";

    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN = "GITHUB_TOKEN";

    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_PASSWORD = "GITHUB_PASSWORD";

    /**
     * @return the GitHub username
     */
    public static String getUsername() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_USERNAME);
    }

    /**
     * @return the GitHub token
     */
    public static String getToken() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN);
    }

    /**
     * @return the GitHub password
     */
    public static String getPassword() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_PASSWORD);
    }
}
