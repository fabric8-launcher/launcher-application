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
package io.fabric8.forge.generator.github;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Results from creating a git repo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateGitRepoStatusDTO {
    private String gitUrl;
    private String gitHtmlUrl;
    private String gitOwnerName;
    private String organisationName;
    private String repositoryName;

    public CreateGitRepoStatusDTO() {
    }

    public CreateGitRepoStatusDTO(String gitUrl, String gitHtmlUrl, String gitOwnerName, String organisationName, String repositoryName) {
        this.gitUrl = gitUrl;
        this.gitHtmlUrl = gitHtmlUrl;
        this.gitOwnerName = gitOwnerName;
        this.organisationName = organisationName;
        this.repositoryName = repositoryName;
    }

    @Override
    public String toString() {
        return "CreateGitRepoStatusDTO{" +
                "gitUrl='" + gitUrl + '\'' +
                ", gitHtmlUrl='" + gitHtmlUrl + '\'' +
                ", gitOwnerName='" + gitOwnerName + '\'' +
                ", organisationName='" + organisationName + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                '}';
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getGitHtmlUrl() {
        return gitHtmlUrl;
    }

    public void setGitHtmlUrl(String gitHtmlUrl) {
        this.gitHtmlUrl = gitHtmlUrl;
    }

    public String getGitOwnerName() {
        return gitOwnerName;
    }

    public void setGitOwnerName(String gitOwnerName) {
        this.gitOwnerName = gitOwnerName;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
}
