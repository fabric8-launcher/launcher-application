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
package io.fabric8.forge.generator.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.utils.Strings;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;

import static io.fabric8.forge.generator.utils.StringHelpers.createdAtText;

/**
 * Represents a github repository you can pick
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GitRepositoryDTO implements Comparable<GitRepositoryDTO> {
    private String id;
    private String name;
    private String description;

    public GitRepositoryDTO(String key, GHRepository repository) {
        this.id = key;
        this.name = repository.getName();
        if (Strings.isNullOrBlank(name)) {
            name = repository.getFullName();
        }
        if (Strings.isNullOrBlank(name)) {
            name = key;
        }
        description = repository.getDescription();
        if (Strings.isNullOrBlank(description) || key.equals(description)) {
            try {
                description = createdAtText(repository.getCreatedAt());
            } catch (IOException e) {
                // ignore
            }
        }
        if (Strings.isNullOrBlank(description)) {
            description = repository.getHomepage();
        }
    }

    public static List<GitRepositoryDTO> asListOfRepositories(Iterable<String> names) {
        List<GitRepositoryDTO> answer = new ArrayList<>();
        for (String name : names) {
            answer.add(new GitRepositoryDTO(name));
        }
        return answer;
    }

    public GitRepositoryDTO() {
    }

    public GitRepositoryDTO(String name) {
        this.id = name;
        this.name = name;
    }

    public GitRepositoryDTO(String name, String description) {
        this.id = name;
        this.name = name;
        this.description = description;
        if (Objects.equals(this.name, this.description)) {
            this.description = "";
        }
    }

    public GitRepositoryDTO(GHOrganization organization) throws IOException {
        this.id = organization.getLogin();
        this.name = organization.getName();
    }

    public boolean isValid() {
        return Strings.isNotBlank(id) && Strings.isNotBlank(name);
    }

    @Override
    public String toString() {
        return "GitRepositoryDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitRepositoryDTO that = (GitRepositoryDTO) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(GitRepositoryDTO that) {
        return this.name.compareTo(that.name);
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
}
