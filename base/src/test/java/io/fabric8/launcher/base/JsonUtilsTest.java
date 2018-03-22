package io.fabric8.launcher.base;

import java.util.Arrays;
import java.util.Collections;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class JsonUtilsTest {

    @Test
    public void testToJsonArrayBuilder() {
        JsonArrayBuilder arrayBuilder = JsonUtils.toJsonArrayBuilder(Arrays.asList("A", "B", "C"));
        assertThat(arrayBuilder).isNotNull();
        JsonArray jsonArray = arrayBuilder.build();
        assertThat(jsonArray.stream().map(JsonString.class::cast).map(JsonString::getString)).containsSequence("A", "B", "C");
    }

    @Test
    public void testToJsonObjectBuilder() {
        JsonObjectBuilder objectBuilder = JsonUtils.toJsonObjectBuilder(Collections.singletonMap("key", "value"));
        assertThat(objectBuilder).isNotNull();
        JsonObject jsonObject = objectBuilder.build();
        assertThat(jsonObject.getString("key")).isEqualTo("value");
    }

}
