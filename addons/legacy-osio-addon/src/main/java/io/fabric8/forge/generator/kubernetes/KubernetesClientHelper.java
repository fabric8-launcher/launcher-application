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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.extensions.Configs;
import io.fabric8.kubernetes.api.spaces.Space;
import io.fabric8.kubernetes.api.spaces.Spaces;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class KubernetesClientHelper {
    private static final transient Logger LOG = LoggerFactory.getLogger(KubernetesClientHelper.class);

    private final KubernetesClient kubernetesClient;

    protected KubernetesClientHelper(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Returns the current users kubernetes/openshift user name
     */
    public String getUserName() {
        OpenShiftClient oc = getOpenShiftClientOrNull();
        if (oc != null) {
            User user = oc.users().withName("~").get();
            if (user == null) {
                LOG.warn("Failed to find current logged in user!");
            } else {
                String answer = KubernetesHelper.getName(user);
                if (Strings.isNullOrBlank(answer)) {
                    LOG.warn("No name for User " + user);
                } else {
                    return answer;
                }
            }
        }

        // TODO needs to use the current token to find the current user name
        return Configs.currentUserName();
    }

    public OpenShiftClient getOpenShiftClientOrNull() {
        return new Controller(kubernetesClient).getOpenShiftClientOrNull();
    }

    /**
     * Returns a unique key specific to the current user request
     */
    public String getUserCacheKey() {
        String answer = kubernetesClient.getConfiguration().getOauthToken();
        if (Strings.isNotBlank(answer)) {
            return answer;
        }
        LOG.warn("Could not find the OAuthToken to use as a user cache key!");
        return "TODO";
    }

    /**
     * Returns the namespace used to discover services like gogs and gitlab when on premise
     */
    public String getDiscoveryNamespace(String createInNamespace) {
        if (Strings.isNotBlank(createInNamespace)) {
            return createInNamespace;
        }
        String namespace = System.getenv(EnvironmentVariables.NAMESPACE);
        if (Strings.isNotBlank(namespace)) {
            return namespace;
        }
        namespace = kubernetesClient.getNamespace();
        if (Strings.isNotBlank(namespace)) {
            return namespace;
        }
        return KubernetesHelper.defaultNamespace();
    }

    public List<SpaceDTO> loadSpaces(String namespace) {
        List<SpaceDTO> answer = new ArrayList<>();
        if (namespace != null) {
            try {
                Spaces spacesValue = Spaces.load(kubernetesClient, namespace);
                if (spacesValue != null) {
                    SortedSet<Space> spaces = spacesValue.getSpaceSet();
                    for (Space space : spaces) {
                        answer.add(new SpaceDTO(space.getName(), space.getName()));
                    }
                }
            } catch (Exception e) {
                LOG.warn("Failed to load spaces: " + e, e);
            }
        }
        return answer;
    }

    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }
}
