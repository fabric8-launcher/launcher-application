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

/**
 */
public class EnvironmentVariables {
    public static final String WIT_URL = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("WIT_URL");

    public static final String AUTH_URL = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("AUTH_URL");

    // TODO: Replace this with a cluster entry in the openshift-clusters.yaml file
    public static final String OPENSHIFT_API_URL = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("OPENSHIFT_API_URL");

    public static final String JENKINS_URL = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("JENKINS_URL");
}
