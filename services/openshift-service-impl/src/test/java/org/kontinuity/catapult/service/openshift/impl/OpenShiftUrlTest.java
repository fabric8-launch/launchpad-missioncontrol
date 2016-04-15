package org.kontinuity.catapult.service.openshift.impl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.kontinuity.catapult.base.test.EnvironmentVariableController.setEnv;
import static org.kontinuity.catapult.service.openshift.api.OpenShiftUrl.get;

/**
 * Tests that we get the OpenShift API URL in the correct precedence (lower number gets priority):
 * <p>
 * 1) System Property {@link org.kontinuity.catapult.service.openshift.api.OpenShiftEnvVarSysPropNames#OPENSHIFT_URL}
 * 2) Environment Variable {@link org.kontinuity.catapult.service.openshift.api.OpenShiftEnvVarSysPropNames#OPENSHIFT_URL}
 * 3) Default URL (https://localhost:8443)
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class OpenShiftUrlTest {

    private static final String ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL = "KONTINUITY_CATAPULT_OPENSHIFT_URL";
    private static final String TEST_OPENSHIFT_URL = "https://katapult-it-test:8443";
    private static final String DEFAULT_OPENSHIFT_URL = "https://localhost:8443";

    @Test
    @Ignore("If setting the env var to run the test it will fail.")
    public void openShiftUrlFromEnvVar() {
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, get());
        } finally {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftUrlFromSysPropNoEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, "");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, get());
        } finally {
            if (oldOpenShiftProperty != null) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftUrlSysPropOverridesEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, "shouldBeOverriddenBySysPropValue");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, get());
        } finally {
            if (oldOpenShiftProperty != null) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftUrlFromDefaultOnNoSysPropOrEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, "");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, "");
            Assert.assertEquals(DEFAULT_OPENSHIFT_URL, get());
        } finally {
            if (oldOpenShiftProperty != null && !oldOpenShiftProperty.isEmpty()) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_URL, oldOpenShiftUrlEnv);
        }
    }
}
