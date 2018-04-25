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

import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;

import static io.fabric8.launcher.base.EnvironmentSupport.INSTANCE;
import static io.fabric8.utils.URLUtils.pathJoin;

/**
 */
public final class OsioConfigs {

    private OsioConfigs() {
        throw new IllegalAccessError("Constants class");
    }

    private static final String WIT_URL = INSTANCE.getEnvVarOrSysProp("WIT_URL", "https://api.openshift.io");

    private static final String AUTH_URL = INSTANCE.getEnvVarOrSysProp("AUTH_URL", "https://auth.openshift.io");

    private static final String OPENSHIFT_API_URL = INSTANCE.getEnvVarOrSysProp("OPENSHIFT_API_URL", "https://f8osoproxy-test-dsaas-production.09b5.dsaas.openshiftapps.com");

    private static final String JENKINS_URL = INSTANCE.getEnvVarOrSysProp("JENKINS_URL", "https://jenkins.openshift.io");

    private static final OpenShiftCluster OSIO_CLUSTER = new OpenShiftCluster("osio", "osio", OPENSHIFT_API_URL, OPENSHIFT_API_URL);

    public static String getWitUrl() {
        return WIT_URL;
    }

    public static String getAuthUrl() {
        return AUTH_URL;
    }

    public static String getJenkinsUrl() {
        return JENKINS_URL;
    }

    public static OpenShiftCluster getOpenShiftCluster() {
        return OSIO_CLUSTER;
    }

    public static String getJenkinsWebhookUrl() {
        return pathJoin(getJenkinsUrl(), "/github-webhook/");
    }
}
