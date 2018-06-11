package io.fabric8.launcher.base;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import static io.fabric8.launcher.base.EnvironmentSupport.getBooleanEnvVarOrSysProp;
import static io.fabric8.launcher.base.EnvironmentSupport.getEnvVarOrSysProp;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class EnvironmentSupportTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnvVarOrSysPropNameShouldNotBeNull() {
        getEnvVarOrSysProp(null, "dummy");
    }

    @Test
    public void testGetEnvVarOrSysProp() {
        String pathSeparator = getEnvVarOrSysProp("path.separator");
        Assert.assertEquals(File.pathSeparator, pathSeparator);
    }

    @Test
    public void testGetEnvVarOrSysPropDefault() {
        String pathSeparator = getEnvVarOrSysProp("path.separator.foo", File.pathSeparator);
        Assert.assertEquals(File.pathSeparator, pathSeparator);
    }

    @Test
    public void testGetEnvVarOrSysPropBoolean() {
        boolean value = getBooleanEnvVarOrSysProp("value");
        Assert.assertFalse(value);
    }

}
