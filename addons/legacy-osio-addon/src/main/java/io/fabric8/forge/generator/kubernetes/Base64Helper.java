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
package io.fabric8.forge.generator.kubernetes;

import java.util.Base64;

import io.fabric8.utils.Strings;

/**
 */
public class Base64Helper {
    public static String base64decode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return new String(Base64.getDecoder().decode(text));
    }

    public static String base64encode(String text) {
        if (Strings.isNullOrBlank(text)) {
            return text;
        }
        return Base64.getEncoder().encodeToString(text.getBytes());
    }
}
