package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.junit.Assume;

/**
 * Used to obtain the GitHub credentials for testing purposes
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubTestingCredentials {

    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_USERNAME = "GITHUB_USERNAME";
    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN = "GITHUB_TOKEN";

    private GitHubTestingCredentials(){
        // No instances
    }

    /**
     * @return the GitHub username
     */
    public static String getUsername(){
        return GitHubTestingCredentials.getEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_USERNAME);
    }

    /**
     * @return the GitHub token
     */
    public static String getToken(){
        return GitHubTestingCredentials.getEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN);
    }

    private static String getEnvVarOrSysProp(final String envVarOrSysProp){
        String value = System.getProperty(envVarOrSysProp);
        if(value==null){
            value = System.getenv(envVarOrSysProp);
        }
        Assume.assumeNotNull("Could not find required env var or sys prop " + envVarOrSysProp, value);
        return value;
    }

}
