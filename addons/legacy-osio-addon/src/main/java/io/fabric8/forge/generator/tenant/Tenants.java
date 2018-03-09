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
package io.fabric8.forge.generator.tenant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import io.fabric8.forge.generator.utils.WebClientHelpers;
import io.fabric8.utils.Objects;
import io.fabric8.utils.Strings;
import io.fabric8.utils.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.fabric8.forge.generator.EnvironmentVariables.getWitApiURL;

/**
 */
public final class Tenants {

    private static final transient Logger LOG = LoggerFactory.getLogger(Tenants.class);

    private Tenants() {
        throw new IllegalAccessError("Utility class");
    }

    public static TenantResultsDTO loadTenant(String authHeader) {
        String witAPI = getWitApiURL();
        String tenantUrl = URLUtils.pathJoin(witAPI, "/api/user/services");
        LOG.debug("Loading user tenant information from " + tenantUrl);
        Client client = WebClientHelpers.createClientWihtoutHostVerification();
        TenantResultsDTO results = client.target(tenantUrl)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .get(TenantResultsDTO.class);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded results: " + results);
        }
        return results;
    }

    public static List<NamespaceDTO> loadNamespaces(String authHeader) {
        TenantResultsDTO results = loadTenant(authHeader);
        if (results != null) {
            TenantDataDTO data = results.getData();
            if (data != null) {
                TenantAttributesDTO attributes = data.getAttributes();
                if (attributes != null) {
                    List<NamespaceDTO> namespaces = attributes.getNamespaces();
                    if (namespaces != null) {
                        return namespaces;
                    }
                }
            }
        }
        return new ArrayList<>();
    }


    public static String findDefaultUserNamespace(List<NamespaceDTO> namespaces) {
        List<String> list = userNamespaces(namespaces);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static String findDefaultJenkinsNamespace(List<NamespaceDTO> namespaces) {
        List<String> list = jenkinsNamespaces(namespaces);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Returns the user namespaces
     */
    public static List<String> userNamespaces(List<NamespaceDTO> namespaces) {
        return namespacesForType(namespaces, "user");
    }

    /**
     * Returns the user namespaces
     */
    public static List<String> jenkinsNamespaces(List<NamespaceDTO> namespaces) {
        return namespacesForType(namespaces, "jenkins");
    }

    protected static List<String> namespacesForType(List<NamespaceDTO> namespaces, String type) {
        List<String> answer = new ArrayList<>();
        if (namespaces != null) {
            for (NamespaceDTO namespace : namespaces) {
                String name = namespace.getName();
                if (Strings.isNotBlank(name)) {
                    if (Objects.equal(type, namespace.getType())) {
                        answer.add(namespace.getName());
                    }
                }
            }
        }
        Collections.sort(answer);
        return answer;
    }


}
