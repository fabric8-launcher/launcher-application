/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.generator.utils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 */
public final class JsonHelper {

    private JsonHelper() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Navigates the given tree with the given sequence of paths
     */
    public static JsonNode navigate(JsonNode tree, String... paths) {
        JsonNode node = tree;
        for (String path : paths) {
            if (node == null) {
                return null;
            }
            node = node.get(path);
        }
        return node;
    }

    public static String textValue(JsonNode node, String name) {
        if (node != null) {
            JsonNode value = node.get(name);
            if (value != null) {
                return value.textValue();
            }
        }
        return null;
    }
}
