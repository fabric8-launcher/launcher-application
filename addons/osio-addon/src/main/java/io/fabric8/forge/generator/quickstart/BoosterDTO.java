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
package io.fabric8.forge.generator.quickstart;


import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.utils.Objects;
import io.openshift.booster.catalog.rhoar.Mission;
import io.openshift.booster.catalog.rhoar.RhoarBooster;
import io.openshift.booster.catalog.rhoar.Runtime;

/**
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BoosterDTO implements Comparable<BoosterDTO> {
    private transient RhoarBooster booster;
    private transient Mission mission;
    private transient Runtime runtime;
    private String id;
    private String name;
    private String description;
    private String missionId;
    private String missionName;
    private String runtimeId;
    private String runtimeName;

    public BoosterDTO() {
    }

    public BoosterDTO(RhoarBooster booster) {
        this.booster = booster;
        this.id = booster.getId();
        this.name = booster.getName();
        this.description = booster.getDescription();
        this.mission = booster.getMission();
        this.runtime = booster.getRuntime();
        if (mission != null) {
            this.missionId = mission.getId();
            this.missionName = mission.getName();
        }
        if (runtime != null) {
            this.runtimeId = runtime.getId();
            this.runtimeName = runtime.getName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoosterDTO that = (BoosterDTO) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BoosterDTO{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(BoosterDTO that) {
        int answer = Objects.compare(this.runtimeName, that.runtimeName);
        if (answer == 0) {
            answer = Objects.compare(this.name, that.name);
            if (answer == 0) {
                answer = Objects.compare(this.missionName, that.missionName);
                if (answer == 0) {
                    answer = Objects.compare(this.missionName, that.missionName);
                    if (answer == 0) {
                        answer = Objects.compare(this.id, that.id);
                    }
                }
            }
        }
        return answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }

    public Mission mission() {
        return mission;
    }

    public Runtime runtime() {
        return runtime;
    }

    public RhoarBooster booster() {
        return booster;
    }
}
