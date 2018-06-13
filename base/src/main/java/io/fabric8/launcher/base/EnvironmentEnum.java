package io.fabric8.launcher.base;

import static io.fabric8.launcher.base.EnvironmentSupport.getBooleanEnvVarOrSysProp;
import static io.fabric8.launcher.base.EnvironmentSupport.getEnvVarOrSysProp;
import static io.fabric8.launcher.base.EnvironmentSupport.getRequiredEnvVarOrSysProp;

/**
 * Interface to be implemented by enums containing properties
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface EnvironmentEnum {

    default String propertyKey() {
        return toString();
    }

    default String value() {
        return getEnvVarOrSysProp(propertyKey());
    }

    default String value(String defaultValue) {
        return getEnvVarOrSysProp(propertyKey(), defaultValue);
    }

    default String valueRequired() {
        return getRequiredEnvVarOrSysProp(propertyKey());
    }

    default boolean booleanValue() {
        return getBooleanEnvVarOrSysProp(propertyKey());
    }

    default boolean isSet() {
        return value() != null;
    }
}
