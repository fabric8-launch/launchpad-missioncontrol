package org.kontinuity.catapult.service.openshift.test;

import org.kontinuity.catapult.base.EnvironmentSupport;
import org.kontinuity.catapult.base.identity.Identity;
import org.kontinuity.catapult.base.identity.IdentityFactory;

/**
 * Used to obtain the OpenShift credentials from the environment
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftTestCredentials {

    private OpenShiftTestCredentials() {
        // No instances
    }

    private static final String NAME_ENV_VAR_SYSPROP_OPENSHIFT_USERNAME = "CATAPULT_OPENSHIFT_USERNAME";
    private static final String NAME_ENV_VAR_SYSPROP_OPENSHIFT_PASSWORD = "CATAPULT_OPENSHIFT_PASSWORD";

    /**
     * @return the Openshift token
     */
    public static Identity getToken() {
        return IdentityFactory.createFromUserPassword(
                EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_OPENSHIFT_USERNAME),
                EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(NAME_ENV_VAR_SYSPROP_OPENSHIFT_PASSWORD));
    }
}
