package io.fabric8.launcher.base;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class YamlUtilsTest {

    @Test
    void should_serialize_yaml() throws Exception {
        String content = "- id: 1\n  name: foo\n- id: 2\n  name: bar";
        List<TestClass> tests = YamlUtils.readList(new StringReader(content), TestClass.class);
        assertThat(tests).hasSize(2);
        assertThat(tests.get(0))
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "foo");
        assertThat(tests.get(1))
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "bar");
    }

    private static class TestClass {
        private int id;

        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}