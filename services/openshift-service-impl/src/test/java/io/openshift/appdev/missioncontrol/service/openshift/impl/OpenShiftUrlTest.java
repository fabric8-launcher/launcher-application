package io.openshift.appdev.missioncontrol.service.openshift.impl;

import org.junit.Assert;
import org.junit.Test;

import static io.openshift.appdev.missioncontrol.base.test.EnvironmentVariableController.setEnv;
import static io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftSettings.getOpenShiftApiUrl;
import static io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftSettings.getOpenShiftConsoleUrl;

/**
 * Tests that we get the OpenShift API URL in the correct precedence (lower number gets priority):
 * <p>
 * 1) System Property
 * 2) Environment Variable
 * 3) Default URL (https://localhost:8443)
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class OpenShiftUrlTest {

    private static final String ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_API_URL";

    private static final String ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL = "LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL";

    private static final String TEST_OPENSHIFT_URL = "https://katapult-it-test:8443";

    @Test
    public void openShiftApiUrlFromEnvVar() {
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, getOpenShiftApiUrl());
        } finally {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftConsoleUrlFromEnvVar() {
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, getOpenShiftConsoleUrl());
        } finally {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftApiUrlFromSysPropNoEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, "");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, getOpenShiftApiUrl());
        } finally {
            if (oldOpenShiftProperty != null) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftConsoleUrlFromSysPropNoEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, "");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, getOpenShiftConsoleUrl());
        } finally {
            if (oldOpenShiftProperty != null) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftApiUrlSysPropOverridesEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, "shouldBeOverriddenBySysPropValue");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, getOpenShiftApiUrl());
        } finally {
            if (oldOpenShiftProperty != null) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_API_URL, oldOpenShiftUrlEnv);
        }
    }

    @Test
    public void openShiftConsoleUrlSysPropOverridesEnvVar() {
        String oldOpenShiftProperty = System.getProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL);
        String oldOpenShiftUrlEnv = System.getenv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL);
        try {
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, "shouldBeOverriddenBySysPropValue");
            System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, TEST_OPENSHIFT_URL);
            Assert.assertEquals(TEST_OPENSHIFT_URL, getOpenShiftConsoleUrl());
        } finally {
            if (oldOpenShiftProperty != null) {
                System.setProperty(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, oldOpenShiftProperty);
            }
            setEnv(ENV_VAR_SYSPROP_NAME_OPENSHIFT_CONSOLE_URL, oldOpenShiftUrlEnv);
        }
    }
}
