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

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.utils.Strings;
import org.jboss.forge.addon.ui.context.UIContext;

/**
 */
public class TokenHelper {

    public static String getMandatoryAuthHeader(UIContext context) {
        String authToken = getAuthHeader(context);
        if (Strings.isNullOrBlank(authToken)) {
            throw new WebApplicationException("No authorization header available", Response.Status.UNAUTHORIZED);
        }
        return authToken;
    }

    public static String getAuthHeader(UIContext context) {
        String authToken = headerToString(context.getAttributeMap().get(HttpHeaders.AUTHORIZATION));
        if (Strings.isNullOrBlank(authToken)) {
            authToken = System.getenv(EnvironmentVariables.TESTING_OAUTH_HEADER);
        }
        return authToken;
    }

    private static String headerToString(Object authorization) {
        if (authorization == null) {
            return null;
        }
        if (authorization instanceof List) {
            List list = (List) authorization;
            if (!list.isEmpty()) {
                return headerToString(list.get(0));
            }
        }
        if (authorization instanceof Object[]) {
            Object[] array = (Object[]) authorization;
            if (array.length > 0) {
                return headerToString(array[0]);
            }
        }
        return authorization.toString();
    }

    public static String getMandatoryTokenFor(KeycloakEndpoint endpoint, String authHeader) {
        KeycloakClient client = new KeycloakClient();
        String token = client.getTokenFor(endpoint, authHeader);
        if (Strings.isNullOrBlank(token)) {
            throw new WebApplicationException("No auth token available for " + endpoint.getName(), Response.Status.UNAUTHORIZED);
        }
        return token;
    }
}
