package io.fabric8.launcher.base.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author <a href="mailto:pmuir@redhat.com">Pete Muir</a>
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class EnvironmentVariableController {

    private EnvironmentVariableController() {
        // No instances
    }

    /**
     * Adapted from:
     * http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
     */
    public static void setEnv(final String name, final String value) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Class<?> variableClass = Class.forName("java.lang.ProcessEnvironment$Variable");
            Class<?> valueClass = Class.forName("java.lang.ProcessEnvironment$Value");

            Map<Object, Object> envs = (Map<Object, Object>) theEnvironmentField.get(null);
            Method variableValueOfMethod = variableClass.getMethod("valueOf", String.class);
            variableValueOfMethod.setAccessible(true);
            Object variable = variableValueOfMethod.invoke(null, name);
            if (value == null) {
                envs.remove(variable);
            } else {
                Method valueValueOfMethod = valueClass.getMethod("valueOf", String.class);
                valueValueOfMethod.setAccessible(true);
                Object valueObj = valueValueOfMethod.invoke(null, value);
                envs.put(variable, valueObj);
            }
        } catch (Exception e1) {
            //NOOP
            e1.printStackTrace();
        }
    }

}
