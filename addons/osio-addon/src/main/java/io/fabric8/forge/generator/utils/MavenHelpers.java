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
package io.fabric8.forge.generator.utils;

import java.io.File;

import io.fabric8.forge.addon.utils.CommandHelpers;
import io.fabric8.forge.generator.che.CheStackDetector;
import io.fabric8.utils.Files;
import org.jboss.forge.addon.ui.context.UIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 */
public class MavenHelpers {
    private static final transient Logger LOG = LoggerFactory.getLogger(MavenHelpers.class);

    /**
     * Loads the pom file if present
     */
    public static PomFileXml findPom(UIContext context, org.jboss.forge.addon.projects.Project project, File pomFile) {
        if (pomFile == null && project != null) {
            pomFile = CommandHelpers.getProjectContextFile(context, project, "pom.xml");
        }
        if (Files.isFile(pomFile)) {
            Document doc = null;
            try {
                doc = CheStackDetector.parseXmlFile(pomFile);
            } catch (Exception e) {
                LOG.debug("Failed to parse " + pomFile + " with: " + e, e);
            }
            return new PomFileXml(pomFile, doc);
        }
        return null;
    }
}
