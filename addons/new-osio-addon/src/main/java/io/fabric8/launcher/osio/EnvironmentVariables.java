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

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.utils.URLUtils;

/**
 */
public interface EnvironmentVariables {

    interface ExternalServices {
        static String getTenantIdentityURL() {
            return URLUtils.pathJoin(getWitURL(), "/api/user");
        }

        static String getTenantNamespacesURL() {
            return URLUtils.pathJoin(getWitURL(), "/api/user/services");
        }

        static String getGithubTokenURL() {
            return URLUtils.pathJoin(EnvironmentVariables.getAuthURL(), "/api/token?for=https://github.com");
        }

        ;
    }

    static String getWitURL() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("WIT_URL", "https://api.openshift.io");
    }

    static String getAuthURL() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("AUTH_URL", "https://auth.openshift.io");
    }

    // TODO: Replace this with a cluster entry in the openshift-clusters.yaml file
    static String getOpenShiftApiURL() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("OPENSHIFT_API_URL", "https://f8osoproxy-test-dsaas-production.09b5.dsaas.openshiftapps.com");
    }

    static String getJenkinsUrl() {
        return EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("JENKINS_URL", "https://jenkins.openshift.io");
    }
}
