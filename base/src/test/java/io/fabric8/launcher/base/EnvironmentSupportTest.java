package io.fabric8.launcher.base;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class EnvironmentSupportTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnvVarOrSysPropNameShouldNotBeNull() {
        EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(null, "dummy");
    }

    @Test
    public void testGetEnvVarOrSysProp() {
        String pathSeparator = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("path.separator");
        Assert.assertEquals(File.pathSeparator, pathSeparator);
    }

    @Test
    public void testGetEnvVarOrSysPropDefault() {
        String pathSeparator = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("path.separator.foo", File.pathSeparator);
        Assert.assertEquals(File.pathSeparator, pathSeparator);
    }

    @Test
    public void testGetEnvVarOrSysPropBoolean() {
        boolean value = EnvironmentSupport.INSTANCE.getBooleanEnvVarOrSysProp("value");
        Assert.assertFalse(value);
    }

}
