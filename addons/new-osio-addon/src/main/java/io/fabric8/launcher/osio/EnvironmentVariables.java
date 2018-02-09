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
package io.fabric8.launcher.osio;

import static io.fabric8.launcher.base.EnvironmentSupport.INSTANCE;
import static io.fabric8.utils.URLUtils.pathJoin;

/**
 */
public interface EnvironmentVariables {

    interface ExternalServices {
        static String getTenantIdentityURL() {
            return pathJoin(getWitURL(), "/api/user");
        }

        static String getTenantNamespacesURL() {
            return pathJoin(getWitURL(), "/api/user/services");
        }

        static String getTokenForURL() {
            return pathJoin(getAuthURL(), "/api/token?for=");
        }

        static String getGithubTokenURL() {
            return getTokenForURL() + "https://github.com";
        }

        static String getJenkinsWebhookURL() {
            return pathJoin(getJenkinsUrl(), "/github-webhook/");
        }

    }

    static String getWitURL() {
        return INSTANCE.getEnvVarOrSysProp("WIT_URL", "https://api.openshift.io");
    }

    static String getAuthURL() {
        return INSTANCE.getEnvVarOrSysProp("AUTH_URL", "https://auth.openshift.io");
    }

    // TODO: Replace this with a cluster entry in the openshift-clusters.yaml file
    static String getOpenShiftApiURL() {
        return INSTANCE.getEnvVarOrSysProp("OPENSHIFT_API_URL", "https://f8osoproxy-test-dsaas-production.09b5.dsaas.openshiftapps.com");
    }

    static String getJenkinsUrl() {
        return INSTANCE.getEnvVarOrSysProp("JENKINS_URL", "https://jenkins.openshift.io");
    }
}
