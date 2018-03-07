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
package io.fabric8.forge.generator.keycloak;


import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.utils.URLUtils;

public enum KeycloakEndpoint {
    // jwt.io - decode token / json token
    // http://prod-preview.openshift.io/home
    // http://sso.prod-preview.openshift.io/auth/realms/fabric8/account
    // authorization: Bearer <ACCESS_TOKEN>
    // http://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity
    GET_GITHUB_TOKEN("GitHub", URLUtils.pathJoin(EnvironmentVariables.getAuthApiURL(), "/api/token?for=https://github.com"));

    private final String name;
    private final String endpoint;

    KeycloakEndpoint(String name, String url) {
        this.name = name;
        this.endpoint = url;
    }

    public String getName() {
        return name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return endpoint;
    }

}