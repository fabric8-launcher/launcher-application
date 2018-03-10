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

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.forge.generator.utils.JsonHelper;
import io.fabric8.forge.generator.utils.WebClientHelpers;
import io.fabric8.utils.Strings;
import io.fabric8.utils.URLUtils;
import org.jboss.forge.addon.ui.context.UIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public final class ProfileSettings {

    private static final transient Logger LOG = LoggerFactory.getLogger(ProfileSettings.class);

    private ProfileSettings() {
        throw new IllegalAccessError("Utility class");
    }

    public static ProfileSettingsDTO loadProfileSettings(String authHeader) {
        if (Strings.isNullOrBlank(authHeader)) {
            return null;
        }
        String witAPI = EnvironmentVariables.getWitApiURL();
        String userSettingsUrl =  URLUtils.pathJoin(witAPI, "/api/user");
        String json = null;
        try {
            Client client = WebClientHelpers.createClientWihtoutHostVerification();
            json = client.target(userSettingsUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", authHeader)
                    .get(String.class);
        } catch (Exception e) {
            LOG.warn("Could not find the user settings at " + userSettingsUrl + " due to: " + e, e);
        }
        if (Strings.isNotBlank(json)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = null;
            try {
                tree = mapper.readTree(json);
            } catch (IOException e) {
                LOG.warn("Failed to parse user settings " + e + " with JSON: " + json, e);
            }
            JsonNode boosterCatalog = JsonHelper.navigate(tree, "data", "attributes", "contextInformation", "boosterCatalog");
            if (boosterCatalog != null) {
                ProfileSettingsDTO answer = new ProfileSettingsDTO();
                answer.setCatalogGitRef(JsonHelper.textValue(boosterCatalog, "gitRef"));
                answer.setCatalogGitRepo(JsonHelper.textValue(boosterCatalog, "gitRepo"));
                return answer;
            }
        }
        return null;
    }

    public static void updateAttributeMap(ProfileSettingsDTO profileSettings, UIContext uiContext) {
        if (profileSettings != null) {
            Map<Object, Object> attributeMap = uiContext.getAttributeMap();
            setAttributeIfNotBlank(attributeMap, AttributeMapKeys.CATALOG_GIT_REPOSITORY, profileSettings.getCatalogGitRepo());
            setAttributeIfNotBlank(attributeMap, AttributeMapKeys.CATALOG_GIT_REF, profileSettings.getCatalogGitRef());
        }
    }


    protected static void setAttributeIfNotBlank(Map<Object, Object> attributeMap, String key, String value) {
        if (Strings.isNotBlank(value)) {
            LOG.debug("setting attributeMap " + key + " = " + value);
            attributeMap.put(key, value);
        }
    }

}
