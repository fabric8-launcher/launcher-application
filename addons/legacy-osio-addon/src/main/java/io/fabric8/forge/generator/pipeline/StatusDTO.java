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
package io.fabric8.forge.generator.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;

/**
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StatusDTO {
    private List<String> warnings = new ArrayList<>();

    public void addWarning(String message) {
        warnings.add(message);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    /**
     * Logs a warning and returns it in this status DTO
     */
    public void warning(Logger log, String message) {
        log.warn(message);
        addWarning(message);
    }

    public void warning(Logger log, String message, Exception e) {
        log.warn(message, e);
        addWarning(message);
    }
}
