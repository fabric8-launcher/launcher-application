package io.openshift.launchpad.backend.util;

import java.io.StringReader;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for @see JsonBuilder
 */
public class JsonBuilderTest {

    @Test
    public void shouldBuildJson() {
        //given
        JsonBuilder jsonBuilder = new JsonBuilder().createJson().addInput("field", "someValue");

        //when
        JsonObject result = jsonBuilder.build();

        //then
        String json = "{\"inputs\":[{\"name\":\"field\",\"value\":\"someValue\"}],\"stepIndex\":0}";
        assertJsonEquals(result, json);
    }

    @Test
    public void shouldBuildJsonWithStep() {
        //given
        JsonBuilder jsonBuilder = new JsonBuilder().createJson(4);

        //when
        JsonObject result = jsonBuilder.build();

        //then
        String json = "{\"inputs\":[],\"stepIndex\":4}";
        assertJsonEquals(result, json);
    }

    @Test
    public void shouldBuildJsonWithValuesList() {
        //given
        JsonBuilder jsonBuilder = new JsonBuilder().createJson(12).addInput("field", Arrays.asList("value1", "value2"));

        //when
        JsonObject result = jsonBuilder.build();

        //then
        String json = "{\"inputs\":[{\"name\":\"field\",\"value\": [\"value1\", \"value2\"]}],\"stepIndex\":12}";
        assertJsonEquals(result, json);
    }

    private void assertJsonEquals(JsonObject result, String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            assertEquals(reader.readObject(), result);
        }
    }
}