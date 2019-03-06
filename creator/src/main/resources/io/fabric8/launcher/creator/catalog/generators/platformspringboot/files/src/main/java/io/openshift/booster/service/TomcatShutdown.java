/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster.service;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;

public class TomcatShutdown {

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void shutdown() {
        if (context == null) {
            System.out.println("Tomcat context was not registered. Stopping JVM instead.");
            System.exit(0);
        }

        try {
            System.out.println("Stopping Tomcat context.");
            context.stop();
        } catch (LifecycleException e) {
            System.out.println("Error when stopping Tomcat context. Stopping JVM instead.");
            System.exit(0);
        }
    }

}
