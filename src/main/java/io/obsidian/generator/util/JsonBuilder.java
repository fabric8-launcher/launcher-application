package io.obsidian.generator.util;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to help build json responses
 */
public class JsonBuilder {
    private JsonBuilderFactory factory = Json.createBuilderFactory(null);
    private JsonArrayBuilder arrayBuilder;
    private int stepIndex;

    public JsonBuilder createJson() {
        arrayBuilder = factory.createArrayBuilder();
        return this;
    }

    public JsonBuilder createJson(int stepIndex) {
        createJson();
        this.stepIndex = stepIndex;
        return this;
    }

    public JsonBuilder setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
        return this;
    }

    public JsonBuilder addInput(String name, List<String> value) {
        JsonObjectBuilder objectBuilder = factory.createObjectBuilder();
        objectBuilder.add("name", name);

        if (value.size() == 1)
        {
            objectBuilder.add("value", value.get(0));
        }
        else
        {
            JsonArrayBuilder valueArrayBuilder = factory.createArrayBuilder();
            value.forEach(valueArrayBuilder::add);
            objectBuilder.add("value", valueArrayBuilder);
        }

        arrayBuilder.add(objectBuilder);
        return this;
    }

    public JsonBuilder addInput(String name, String... value) {
        return addInput(name, Arrays.asList(value));
    }

    public JsonObject build() {
        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder();
        jsonObjectBuilder.add("inputs", arrayBuilder);
        jsonObjectBuilder.add("stepIndex", stepIndex);

        return jsonObjectBuilder.build();
    }
}
