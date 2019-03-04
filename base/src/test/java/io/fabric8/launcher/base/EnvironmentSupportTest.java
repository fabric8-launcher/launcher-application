package io.fabric8.launcher.base;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static io.fabric8.launcher.base.EnvironmentSupport.getBooleanEnvVarOrSysProp;
import static io.fabric8.launcher.base.EnvironmentSupport.getEnvVarOrSysProp;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class EnvironmentSupportTest {

    @Test
    void testGetEnvVarOrSysPropNameShouldNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> getEnvVarOrSysProp(null, "dummy"));
    }

    @Test
    void testGetEnvVarOrSysProp() {
        String pathSeparator = getEnvVarOrSysProp("path.separator");
        Assert.assertEquals(File.pathSeparator, pathSeparator);
    }

    @Test
    void testGetEnvVarOrSysPropDefault() {
        String pathSeparator = getEnvVarOrSysProp("path.separator.foo", File.pathSeparator);
        Assert.assertEquals(File.pathSeparator, pathSeparator);
    }

    @Test
    void testGetEnvVarOrSysPropBoolean() {
        boolean value = getBooleanEnvVarOrSysProp("value");
        Assert.assertFalse(value);
    }

}
