package io.openshift.appdev.missioncontrol.web.api;

import io.openshift.appdev.missioncontrol.base.EnvironmentSupport;
import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.IdentityFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public abstract class AbstractResource {

    private static final String LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME";

    private static final String LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD";

    private static final String LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN";

    private static final String LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN = "LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN";

    protected Identity getDefaultOpenShiftIdentity() {
        // Read from the ENV variables
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN);
        if (token != null) {
            return IdentityFactory.createFromToken(token);
        } else {
            String user = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME);
            String password = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD);
            return IdentityFactory.createFromUserPassword(user, password);
        }
    }

    protected Identity getDefaultGithubIdentity() {
        // Try using the provided Github token
        String token = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN);
        return IdentityFactory.createFromToken(token);
    }

    protected boolean useDefaultIdentities() {
        String user = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME);
        String password = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD);
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN);

        return ((user != null && password != null) || token != null);
    }

}
