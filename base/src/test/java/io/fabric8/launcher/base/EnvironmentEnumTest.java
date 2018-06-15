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

    @Test
    public void should_be_set() {
        assertThat(TestEnum.JAVA_VERSION.isSet()).isTrue();
    }

    @Test
    public void should_not_be_set() {
        assertThat(TestEnum.FOO.isSet()).isFalse();
    }

    @Test
    public void should_test_boolean_value() {
        assertThat(TestEnum.HOSTNAME.booleanValue()).isFalse();
    }

    @Test
    public void should_test_boolean_value_default_value() {
        assertThat(TestEnum.FOO.booleanValue(true)).isTrue();
    }

    private enum TestEnum implements EnvironmentEnum {
        HOSTNAME,
        FOO,
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