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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Results from creating a BuildConfig
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateBuildConfigStatusDTO {
    private String namespace;
    private String buildConfigName;
    private String gitUrl;
    private String cheStackId;
    private String organisationJenkinsJobUrl;
    private List<String> gitRepositoryNames;
    private List<GitRepoDTO> gitRepositories;
    private String gitOwnerName;
    private List<String> warnings;

    public CreateBuildConfigStatusDTO() {
    }

    public CreateBuildConfigStatusDTO(String namespace, String buildConfigName, String gitUrl, String cheStackId, String organisationJenkinsJobUrl, List<String> gitRepositoryNames, List<GitRepoDTO> gitRepositories, String gitOwnerName, List<String> warnings) {
        this.namespace = namespace;
        this.buildConfigName = buildConfigName;
        this.gitUrl = gitUrl;
        this.cheStackId = cheStackId;
        this.organisationJenkinsJobUrl = organisationJenkinsJobUrl;
        this.gitRepositoryNames = gitRepositoryNames;
        this.gitRepositories = gitRepositories;
        this.gitOwnerName = gitOwnerName;
        this.warnings = warnings;
    }

    @Override
    public String toString() {
        return "CreateBuildConfigStatusDTO{" +
                "namespace='" + namespace + '\'' +
                ", buildConfigName='" + buildConfigName + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", cheStackId='" + cheStackId + '\'' +
                ", organisationJenkinsJobUrl='" + organisationJenkinsJobUrl + '\'' +
                ", gitRepositoryNames=" + gitRepositoryNames +
                ", gitOwnerName='" + gitOwnerName + '\'' +
                '}';
    }

    public String getCheStackId() {
        return cheStackId;
    }

    public void setCheStackId(String cheStackId) {
        this.cheStackId = cheStackId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getBuildConfigName() {
        return buildConfigName;
    }

    public void setBuildConfigName(String buildConfigName) {
        this.buildConfigName = buildConfigName;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getOrganisationJenkinsJobUrl() {
        return organisationJenkinsJobUrl;
    }

    public void setOrganisationJenkinsJobUrl(String organisationJenkinsJobUrl) {
        this.organisationJenkinsJobUrl = organisationJenkinsJobUrl;
    }

    public List<String> getGitRepositoryNames() {
        return gitRepositoryNames;
    }

    public void setGitRepositoryNames(List<String> gitRepositoryNames) {
        this.gitRepositoryNames = gitRepositoryNames;
    }

    public String getGitOwnerName() {
        return gitOwnerName;
    }

    public void setGitOwnerName(String gitOwnerName) {
        this.gitOwnerName = gitOwnerName;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<GitRepoDTO> getGitRepositories() {
        return gitRepositories;
    }

    public void setGitRepositories(List<GitRepoDTO> gitRepositories) {
        this.gitRepositories = gitRepositories;
    }
}
