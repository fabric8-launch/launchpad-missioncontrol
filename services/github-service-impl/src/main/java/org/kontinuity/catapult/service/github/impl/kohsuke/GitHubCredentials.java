package org.kontinuity.catapult.service.github.impl.kohsuke;

/**
 * Used to obtain the GitHub credentials from the environment
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubCredentials {

    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_USERNAME = "GITHUB_USERNAME";
    private static final String NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN = "GITHUB_TOKEN";

    private GitHubCredentials(){
        // No instances
    }

    /**
     * @return the GitHub username
     */
    public static String getUsername(){
        return GitHubCredentials.getEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_USERNAME);
    }

    /**
     * @return the GitHub token
     */
    public static String getToken(){
        return GitHubCredentials.getEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_GITHUB_TOKEN);
    }

    private static String getEnvVarOrSysProp(final String envVarOrSysProp){
        String value = System.getProperty(envVarOrSysProp);
        if (value == null) {
            value = System.getenv(envVarOrSysProp);
        }
        if (value == null) {
            throw new IllegalStateException("Could not find required env var or sys prop " + envVarOrSysProp);
        }
        return value;
    }

}
