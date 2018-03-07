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

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import io.fabric8.forge.generator.keycloak.KeyCloakFailureException;
import io.fabric8.forge.generator.keycloak.KeycloakEndpoint;
import io.fabric8.forge.generator.keycloak.TokenHelper;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.utils.Strings;

/**
 */
public class GitAccount {
    private String username;

    private final String token;

    private final String password;

    private String email;

    public GitAccount(String username, String token, String password, String email) {
        this.username = username;
        this.token = token;
        this.password = password;
        this.email = email;
    }

    public static GitAccount loadFromSaaS(String authHeader) {
        String jwtToken = authHeader;
        int idx = authHeader.indexOf(' ');
        jwtToken = jwtToken.substring(idx + 1, jwtToken.length());
        try {
            JWT.decode(jwtToken);
        } catch (JWTDecodeException e) {
            throw new KeyCloakFailureException("KeyCloak returned an invalid token. Are you sure you are logged in?", e);
        }
        String authToken = TokenHelper.getMandatoryTokenFor(KeycloakEndpoint.GET_GITHUB_TOKEN, authHeader);
        return new GitAccount(null, authToken, null, null);
    }

    /**
     * Creates a default set of git account details using environment variables for testing
     */
    public static GitAccount createViaEnvironmentVariables(String envVarPrefix) {
        String username = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(envVarPrefix + "_USERNAME");
        String password = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(envVarPrefix + "_PASSWORD");
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(envVarPrefix + "_TOKEN");
        String email = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(envVarPrefix + "_EMAIL");
        return new GitAccount(username, token, password, email);
    }

    public static boolean isValid(GitAccount details) {
        // TODO we should probably test logging in as the user to load their organisations?
        return details != null && details.hasValidData();
    }

    @Override
    public String toString() {
        return "GitAccount{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public String tokenOrPassword() {
        if (Strings.isNotBlank(token)) {
            return token;
        }
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasValidData() {
        return Strings.isNotBlank(token) || Strings.isNotBlank(username)
                && Strings.isNotBlank(email) && (Strings.isNotBlank(password) || Strings.isNotBlank(token));
    }

    public String getUserCacheKey() {
        if (Strings.isNotBlank(username)) {
            return username;
        } else if (Strings.isNotBlank(token)) {
            return "token/" + token;
        }
        throw new IllegalArgumentException("No cache key available for user: " + this);
    }
}