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

import java.net.MalformedURLException;
import java.net.URL;

/**
 */
public class WebHookDetails {
    private final String repositoryName;

    private final String webhookUrl;

    private final String secret;

    private String gitOwnerName;

    public WebHookDetails(String gitOwnerName, String repositoryName, String webhookUrl, String secret) {
        this.gitOwnerName = gitOwnerName;
        this.repositoryName = repositoryName;
        this.webhookUrl = webhookUrl;
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "WebHookDetails{" +
                "gitOwnerName='" + gitOwnerName + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", webhookUrl='" + webhookUrl + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public String getSecret() {
        return secret;
    }

    public URL getWebhookURL() throws MalformedURLException {
        return new URL(webhookUrl);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getGitOwnerName() {
        return gitOwnerName;
    }

    public void setGitOwnerName(String gitOwnerName) {
        this.gitOwnerName = gitOwnerName;
    }
}
