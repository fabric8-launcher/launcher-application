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
package io.fabric8.forge.generator.pipeline;

import java.io.File;
import java.util.Map;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.utils.Files;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.util.ResourceUtil;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.fabric8.forge.generator.AttributeMapKeys.PROJECT_DIRECTORY_FILE;

/**
 */
public abstract class AbstractDevToolsCommand extends AbstractUICommand {
    public static final String CATEGORY = "Obsidian";

    final transient Logger log = LoggerFactory.getLogger(this.getClass());

    protected String getProjectName(UIContext uiContext) {
        return (String) uiContext.getAttributeMap().getOrDefault(AttributeMapKeys.NAME, "");
    }

    public static File getSelectionFolder(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();
        File file = (File) attributeMap.get(PROJECT_DIRECTORY_FILE);
        if (file != null) {
            return file;
        }
        UISelection<Object> selection = context.getSelection();
        if (selection != null) {
            Object object = selection.get();
            if (object instanceof File) {
                File folder = (File) object;
                if (Files.isDirectory(folder)) {
                    return folder;
                }
            } else if (object instanceof Resource) {
                File folder = ResourceUtil.getContextFile((Resource<?>) object);
                if (folder != null && Files.isDirectory(folder)) {
                    return folder;
                }
            }
        }
        Project project = (Project) attributeMap.get(Project.class);
        if (project != null) {
            DirectoryResource root = project.getRoot().reify(DirectoryResource.class);
            File folder = ResourceUtil.getContextFile(root);
            if (folder != null && Files.isDirectory(folder)) {
                return folder;
            }
        }
        return null;
    }

    protected void removeTemporaryFiles(File basedir) {
        // TODO lets replace this with a mechanism to register folders to be removed after execution
/*
        if (basedir != null && basedir.exists()) {
            try {
                Files.recursiveDelete(basedir);
            } catch (Throwable e) {
                log.warn("Failed to delete " + basedir + " due to: " + e, e);
            }
        }
*/
    }
}
