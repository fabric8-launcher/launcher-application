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
    public String getEnvVarOrSysProp(final String envVarOrSysProp) throws IllegalArgumentException {
        if (envVarOrSysProp == null || envVarOrSysProp.isEmpty()) {
            throw new IllegalArgumentException("env var or sysprop name is required");
        }
        String value = System.getProperty(envVarOrSysProp);
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVarOrSysProp);
        }
        // Set empty strings to null per contract
        if (value != null && value.isEmpty()) {
            value = null;
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
     * property in the case both are defined; null if not found.
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     * @throws IllegalArgumentException If the defaultValue is not specified
     */
    public String getEnvVarOrSysProp(final String envVarOrSysProp, String defaultValue) throws IllegalArgumentException {
        if (defaultValue == null || defaultValue.isEmpty()) {
            throw new IllegalArgumentException("default value for " + envVarOrSysProp + " is required");
        }

        String value = getEnvVarOrSysProp(envVarOrSysProp);

        return value == null ? defaultValue : value;
    }

    /**
     * Obtains the environment variable or system property, with preference to the system
     * property in the case both are defined.  Returns true if the value of the variable is
     * equal to the string "true", in all other cases false.
     *
     * @param envVarOrSysProp the environment variable or system property name
     * @return true if the environment variable or system property is equal to "true"
     * property in the case both are defined; null if not found.
     * @throws IllegalArgumentException If the env var or sysprop name is not specified
     */
    public boolean getBooleanEnvVarOrSysProp(final String envVarOrSysProp) throws IllegalArgumentException {
        String value = getEnvVarOrSysProp(envVarOrSysProp);
        try {
            return Boolean.parseBoolean(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
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
        if (value == null || value.isEmpty()) {
            final String errorMessage = MessageFormat.format("Could not find required env var or sys prop {0}", envVarOrSysProp);
            throw new IllegalStateException(errorMessage);
        }
        return value;
    }

}
