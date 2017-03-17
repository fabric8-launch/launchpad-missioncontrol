package org.kontinuity.catapult.service.openshift.test;

import org.kontinuity.catapult.base.EnvironmentSupport;

/**
 * Used to obtain the OpenShift credentials from the environment
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftTestCredentials {

    private OpenShiftTestCredentials() {
        // No instances
    }

    private static final String NAME_ENV_VAR_SYSPROP_OPENSHIFT_TOKEN = "CATAPULT_OPENSHIFT_TOKEN";

    /**
     * @return the Openshift token
     */
    public static String getToken() {
        return EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_OPENSHIFT_TOKEN);
    }
}
