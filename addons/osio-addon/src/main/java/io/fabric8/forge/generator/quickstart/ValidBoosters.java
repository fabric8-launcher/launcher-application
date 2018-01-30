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
package io.fabric8.forge.generator.quickstart;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.openshift.booster.catalog.Booster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ValidBoosters {
    private static final transient Logger LOG = LoggerFactory.getLogger(ValidBoosters.class);

    /**
     * The following is a list of booster IDs we have checked work on OpenShift IO
     *
     * Over time we may have separate lists based on whether we are fabric8 upstream,
     * OSO free tier, OSO paid, OSCP etc
     */
    private static final Set<String> validIds = new HashSet<>(Arrays.asList(
            "health-check_spring-boot_community_booster",
            "health-check_vert.x_community_booster",
            "health-check_wildfly-swarm_community_booster",

            "rest-http_spring-boot_community_booster",
            "rest-http_vert.x_community_booster",
            "rest-http_wildfly-swarm_community_booster",

            "does-not-exist-to-make-auto-PRs-easier;)"
    ));
    /**
     * Returns true if we know this booster works on OpenShift.io properly
     */
    public static boolean validRhoarBooster(Booster booster) {
        String id = booster.getId();
        String name = booster.getName();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Booster id " + id + " name: " + name + " " + booster);
        }

        for (String validId : validIds) {
            if (id.startsWith(validId)) {
                return true;
            }
        }
        return false;
    }
}
