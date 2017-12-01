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
import java.net.URL;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.utils.Strings;
import org.kohsuke.github.GHOrganization;

import static io.fabric8.forge.generator.github.GitHubFacade.MY_PERSONAL_GITHUB_ACCOUNT;
import static io.fabric8.forge.generator.utils.StringHelpers.createdAtText;

/**
 * Represents a github organisation you can pick
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GitOrganisationDTO implements Comparable<GitOrganisationDTO> {
    private String id;
    private String name;
    private String description;
    private String avatarUrl;
    private String htmlUrl;

    public GitOrganisationDTO(String name, String description) {
        this.id = name;
        this.name = name;
        this.htmlUrl = "https://github.com/" + name;
        this.description = description;
        // TODO should we add an avatar for the current user?
    }

    public GitOrganisationDTO(GHOrganization organization, String username) throws IOException {
        this.id = organization.getLogin();
        this.name = organization.getName();
        if (this.name == null) {
            this.name = this.id;
        }
        if (Objects.equals(this.id, username) || Objects.equals(this.name, username)) {
            this.description = MY_PERSONAL_GITHUB_ACCOUNT + " " + createdAtText(organization.getCreatedAt());
        } else {
            this.description = organization.getBlog();
            if (Strings.isNullOrBlank(this.description) || this.name.equals(this.description)) {
                this.description = organization.getLocation();
                if (Strings.isNullOrBlank(this.description)) {
                    try {
                        this.description = createdAtText(organization.getCreatedAt());
                    } catch (IOException e) {
                        // ignore
                    }
                }
                if (Strings.isNullOrBlank(this.description)) {
                    this.description = organization.getBlog();
                }
            }
        }
        this.avatarUrl = organization.getAvatarUrl();
        URL htmlUrl = organization.getHtmlUrl();
        if (htmlUrl != null) {
            this.htmlUrl = htmlUrl.toString();
        }
    }

    public boolean isValid() {
        return Strings.isNotBlank(id) && Strings.isNotBlank(name);
    }

    @Override
    public String toString() {
        return "GitOrganisationDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitOrganisationDTO that = (GitOrganisationDTO) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(GitOrganisationDTO that) {
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
