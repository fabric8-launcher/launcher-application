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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.forge.addon.utils.StopWatch;
import io.fabric8.utils.Files;
import io.fabric8.utils.IOHelpers;
import org.jboss.forge.addon.ui.context.UIContext;

/**
 */
public abstract class AbstractProjectOverviewCommand extends AbstractDevToolsCommand {
    public static final int ROOT_LEVEL = 1;

    protected ProjectOverviewDTO getProjectOverview(UIContext uiContext) {
        StopWatch watch = new StopWatch();
        ProjectOverviewDTO projectOverview = new ProjectOverviewDTO();
        File rootFolder = getSelectionFolder(uiContext);
        if (rootFolder != null) {
            List<FileProcessor> processors = loadFileMatches();
            scanProject(rootFolder, processors, projectOverview, 0, 3);
        }
        log.debug("getProjectOverview took " + watch.taken());
        if (projectOverview.getBuilders().isEmpty()) {
            // lets assume maven for now!
            projectOverview.addBuilder("maven");
        }
        return projectOverview;
    }

    protected List<FileProcessor> loadFileMatches() {
        List<FileProcessor> answer = new ArrayList<>();

        answer.add((overview, file, name, extension, level) -> {
            if (level == ROOT_LEVEL && java.util.Objects.equals(name, "pom.xml")) {
                overview.addBuilder("maven");
                overview.addPerspective("forge");
                // check if we have camel/funktion/and others in the maven project
                try {
                    String text = IOHelpers.readFully(file);
                    // just do a quick scan for dependency names as using forge project API is slower
                    if (text.contains("org.apache.camel")) {
                        overview.addPerspective("camel");
                    }
                    if (text.contains("io.fabric8.funktion")) {
                        overview.addPerspective("funktion");
                    }
                    if (text.contains("fabric8-profiles")) {
                        overview.addPerspective("fabric8-profiles");
                    }
                } catch (IOException e) {
                    // ignore
                }
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (level == ROOT_LEVEL && java.util.Objects.equals(name, "Jenkinsfile")) {
                overview.addBuilder("jenkinsfile");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if ((level == ROOT_LEVEL && java.util.Objects.equals(name, "package.json")) || java.util.Objects
                    .equals(extension, "js")) {
                overview.addBuilder("node");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(extension, "go")) {
                overview.addBuilder("golang");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(name, "Rakefile") || java.util.Objects.equals(extension, "rb")) {
                overview.addBuilder("ruby");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(extension, "swift")) {
                overview.addBuilder("swift");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(name, "urls.py") || java.util.Objects.equals(extension, "wsgi.py")) {
                overview.addBuilder("django");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(extension, "php")) {
                overview.addBuilder("php");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(extension, "cs")) {
                overview.addBuilder("dotnet");
                return true;
            }
            return false;
        });
        answer.add((overview, file, name, extension, level) -> {
            if (java.util.Objects.equals(extension, "sbt") || java.util.Objects.equals(extension, "scala")) {
                overview.addBuilder("sbt");
                return true;
            }
            return false;
        });

        return answer;
    }

    protected void scanProject(File file, List<FileProcessor> processors, ProjectOverviewDTO overview, int level,
                               int maxLevels) {
        if (file.isFile()) {
            String name = file.getName();
            String extension = Files.getExtension(name);
            for (FileProcessor processor : new ArrayList<>(processors)) {
                if (processor.processes(overview, file, name, extension, level)) {
                    processors.remove(processor);
                }
            }
        } else if (file.isDirectory()) {
            int newLevel = level + 1;
            if (newLevel <= maxLevels && !processors.isEmpty()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File child : files) {
                        scanProject(child, processors, overview, newLevel, maxLevels);
                    }
                }
            }
        }
    }

    protected interface FileProcessor {
        boolean processes(ProjectOverviewDTO overview, File file, String name, String extension, int level);
    }

}
