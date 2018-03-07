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

/**
 */
public class CloneRepoAttributes {
    private UserDetails userDetails;
    private File directory;
    private String uri;
    private boolean cloneAll = true;
    private String remote = "origin";

    public CloneRepoAttributes() {
    }

    public CloneRepoAttributes(UserDetails userDetails, String uri, File directory) {
        this.userDetails = userDetails;
        this.directory = directory;
        this.uri = uri;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public boolean isCloneAll() {
        return cloneAll;
    }

    public void setCloneAll(boolean cloneAll) {
        this.cloneAll = cloneAll;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }
}
