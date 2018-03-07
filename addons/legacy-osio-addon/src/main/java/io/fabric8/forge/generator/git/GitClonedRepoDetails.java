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

import java.io.File;

import io.fabric8.project.support.UserDetails;
import org.eclipse.jgit.api.Git;

/**
 */
public class GitClonedRepoDetails {
    private final String gitRepoName;
    private final Git git;
    private final CloneRepoAttributes attributes;

    public GitClonedRepoDetails(String gitRepoName, Git git, CloneRepoAttributes attributes) {
        this.gitRepoName = gitRepoName;
        this.git = git;
        this.attributes = attributes;
    }

    public String getGitRepoName() {
        return gitRepoName;
    }

    public Git getGit() {
        return git;
    }

    public CloneRepoAttributes getAttributes() {
        return attributes;
    }

    public String getGitUrl() {
        return attributes.getUri();
    }

    public UserDetails getUserDetails() {
        return attributes.getUserDetails();
    }

    public File getDirectory() {
        return attributes.getDirectory();
    }
}
