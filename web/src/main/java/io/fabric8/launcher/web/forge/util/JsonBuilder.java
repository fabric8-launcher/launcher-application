/**
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.launcher.web.forge.util;

import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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

        if (value.size() == 1) {
            objectBuilder.add("value", value.get(0));
        } else {
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
