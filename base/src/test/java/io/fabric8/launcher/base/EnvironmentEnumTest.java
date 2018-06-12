package io.fabric8.launcher.base;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class EnvironmentEnumTest {

    @Test
    public void should_return_env_var() {
        assertThat(TestEnum.HOSTNAME.value()).isEqualTo(System.getenv("HOSTNAME"));
    }

    @Test
    public void should_return_property() {
        assertThat(TestEnum.JAVA_VERSION.value()).isEqualTo(System.getProperty("java.version"));
    }

    private enum TestEnum implements EnvironmentEnum {
        HOSTNAME,
        JAVA_VERSION("java.version");

        private final String propertyKey;

        TestEnum() {
            this.propertyKey = name();
        }

        TestEnum(String propertyKey) {
            this.propertyKey = propertyKey;
        }

        @Override
        public String propertyKey() {
            return propertyKey;
        }
    }
}