package io.openshift.appdev.missioncontrol.base.test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
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
        Map<String, String> newenv = new HashMap<String, String>();
        newenv.put(name, value);
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);

            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                //NOOP
            }
        } catch (Exception e1) {
            //NOOP
        }
    }

}
