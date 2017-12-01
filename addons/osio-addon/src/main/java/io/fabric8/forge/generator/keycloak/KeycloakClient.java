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

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.forge.generator.utils.WebClientHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class KeycloakClient {
    private static final transient Logger LOG = LoggerFactory.getLogger(KeycloakClient.class);

    private static final String ACCESS_TOKEN = "access_token";
    private static final String SCOPE = "scope";

    public String getTokenFor(KeycloakEndpoint endpoint, String authHeader) {
        // access_token=token&scope=scope
        String responseBody = getResponseBody(endpoint, authHeader);
        if (responseBody == null) {
            return null;
        }
        responseBody = responseBody.trim();
        Map<String, String> parameter;
        if (responseBody.startsWith("{") && responseBody.endsWith("}")) {
            try {
                parameter = new ObjectMapper().readerFor(Map.class).readValue(responseBody);
            } catch (IOException e) {
                throw new WebApplicationException("Failed to parse JSON token reply: " + e, e);
            }
        } else {
            parameter = UrlHelper.splitQuery(responseBody);
        }
        String token = parameter.get(ACCESS_TOKEN);
        LOG.debug("Token: {}", token);
        String scope = parameter.get(SCOPE);
        LOG.debug("Scope: {}", scope);
        return token;
    }

    private String getResponseBody(KeycloakEndpoint endpoint, String authHeader) {
        try {
            Client client = WebClientHelpers.createClientWihtoutHostVerification();
            return client.target(endpoint.toString())
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", authHeader)
                    .get(String.class);
        } catch (Exception e) {
            throw new KeyCloakFailureException(endpoint, e);
        }
    }

}