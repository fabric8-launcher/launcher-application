package io.fabric8.launcher.base;

import java.text.MessageFormat;

/**
 * Utility class to read state from the environment
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public enum EnvironmentSupport {

    INSTANCE;

    /**
     * Obtains the environment variable or system property, with preference to the system
     * property in the case both are defined.  Returns null if not found.
     *
     * @param envVarOrSysProp the environment variable or system property name
     * @return the environment variable or system property, with preference to the system
     * property in the case both are defined; null if not found.
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     */
    public String getEnvVarOrSysProp(final String envVarOrSysProp, String defaultValue) throws IllegalArgumentException {
        if (envVarOrSysProp == null || envVarOrSysProp.isEmpty()) {
            throw new IllegalArgumentException("env var or sysprop name is required");
        }
        String value = System.getProperty(envVarOrSysProp);
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVarOrSysProp);
        }
        // Set null values and empty strings to default value per contract
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Obtains the environment variable or system property, with preference to the system
     * property in the case both are defined.  Returns null if not found.
     *
     * @param envVarOrSysProp the environment variable or system property name
     * @param defaultValue    the default value to be returned if not available
     * @return the environment variable or system property, with preference to the system
     * property in the case both are defined; Or <code>defaultValue</code> if not found.
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     */
    public String getEnvVarOrSysProp(final String envVarOrSysProp) throws IllegalArgumentException {
        return getEnvVarOrSysProp(envVarOrSysProp, null);
    }

    /**
     * Obtains the environment variable or system property, with preference to the system
     * property in the case both are defined.  Returns true if the value of the variable is
     * equal to the string "true", in all other cases false. If the variable is not found
     * the <code>defaultValue</code> will be returned
     *
     * @param envVarOrSysProp the environment variable or system property name
     * @param defaultValue    the default value to be returned if not available
     * @return true if the environment variable or system property is equal to "true"
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     */
    public boolean getBooleanEnvVarOrSysProp(final String envVarOrSysProp, boolean defaultValue) throws IllegalArgumentException {
        String value = getEnvVarOrSysProp(envVarOrSysProp);
        if (value != null) {
            try {
                return Boolean.parseBoolean(value);
            } catch (IllegalArgumentException | NullPointerException e) {
                // Use default value in case of error
            }
        }
        return defaultValue;
    }

    /**
     * Obtains the environment variable or system property, with preference to the system
     * property in the case both are defined.  Returns true if the value of the variable is
     * equal to the string "true", in all other cases false.
     *
     * @param envVarOrSysProp the environment variable or system property name
     * @return true if the environment variable or system property is equal to "true"
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     */
    public boolean getBooleanEnvVarOrSysProp(final String envVarOrSysProp) throws IllegalArgumentException {
        return getBooleanEnvVarOrSysProp(envVarOrSysProp, false);
    }

    /**
     * Obtains the required environment variable or system property, with preference to the system
     * property in the case both are defined.
     *
     * @param envVarOrSysProp
     * @return
     * @throws IllegalStateException    If the requested environment variable or system property was not found
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     */
    public String getRequiredEnvVarOrSysProp(final String envVarOrSysProp)
            throws IllegalStateException, IllegalArgumentException {
        String value = getEnvVarOrSysProp(envVarOrSysProp);
        if (value == null) {
            final String errorMessage = MessageFormat.format("Could not find required env var or sys prop {0}", envVarOrSysProp);
            throw new IllegalStateException(errorMessage);
        }
        return value;
    }

}
