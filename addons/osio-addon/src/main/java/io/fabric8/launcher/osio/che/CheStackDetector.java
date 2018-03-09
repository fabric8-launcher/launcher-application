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
package io.fabric8.launcher.osio.che;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 */
public final class CheStackDetector {

    private CheStackDetector() {
        throw new IllegalAccessError("Utility class");
    }

    private static final transient Logger LOG = Logger.getLogger(CheStackDetector.class.getName());

    private static Map<String, CheStack> mavenPluginMap = new LinkedHashMap<>();

    private static Map<String, CheStack> mavenDependencyGroupMap = new LinkedHashMap<>();

    static {
        mavenPluginMap.put("org.springframework.boot:spring-boot-maven-plugin", CheStack.SpringBoot);
        mavenPluginMap.put("org.wildfly.swarm:wildfly-swarm-plugin", CheStack.WildFlySwarm);
        mavenPluginMap.put("io.fabric8:vertx-maven-plugin", CheStack.Vertx);

        mavenDependencyGroupMap.put("org.springframework.boot", CheStack.SpringBoot);
        mavenDependencyGroupMap.put("io.vertx", CheStack.Vertx);
        mavenDependencyGroupMap.put("org.wildfly.swarm", CheStack.WildFlySwarm);
    }


    /**
     * Lets detect the default stack to use for the newly created project
     */
    public static CheStack detectCheStack(Path projectLocation) {
        Path pomXml = projectLocation.resolve("pom.xml");
        if (Files.exists(pomXml)) {
            try {
                Document doc = parseXmlFile(pomXml.toFile());
                return detectStackFromPomXml(doc);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while parsing pom.xml", e);
            }
        } else {
            Path packageJson = projectLocation.resolve("package.json");
            if (Files.exists(packageJson)) {
                return CheStack.NodeJS;
            }
        }
        // TODO assume Java?
        return CheStack.JavaCentOS;
    }

    private static Document parseXmlFile(File pomFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(pomFile);
    }

    private static CheStack detectStackFromPomXml(Document doc) {
        // lets find the maven plugins first
        NodeList plugins = doc.getElementsByTagName("plugins");
        if (plugins != null) {
            for (int i = 0, size = plugins.getLength(); i < size; i++) {
                Node item = plugins.item(i);
                if (item instanceof Element) {
                    Element element = (Element) item;
                    String groupId = firstElementText(element, "groupId");
                    String artifactId = firstElementText(element, "artifactId");
                    if (groupId != null && artifactId != null) {
                        CheStack stack = mavenPluginMap.get(groupId + ":" + artifactId);
                        if (stack != null) {
                            return stack;
                        }
                    }
                }
            }
        }
        NodeList dependencies = doc.getElementsByTagName("dependency");
        if (dependencies != null) {
            for (int i = 0, size = dependencies.getLength(); i < size; i++) {
                Node item = dependencies.item(i);
                if (item instanceof Element) {
                    Element element = (Element) item;
                    String groupId = firstElementText(element, "groupId");
                    CheStack stack = mavenDependencyGroupMap.get(groupId);
                    if (stack != null) {
                        return stack;
                    }
                }
            }
        }
        return CheStack.JavaCentOS;
    }

    private static String firstElementText(Element element, String name) {
        Element child = firstChild(element, name);
        if (child != null) {
            return child.getTextContent();
        }
        return null;
    }

    private static Element firstChild(Element element, String name) {
        NodeList nodes = element.getChildNodes();
        if (nodes != null) {
            for (int i = 0, size = nodes.getLength(); i < size; i++) {
                Node item = nodes.item(i);
                if (item instanceof Element) {
                    Element childElement = (Element) item;

                    if (name.equals(childElement.getTagName())) {
                        return childElement;
                    }
                }
            }
        }
        return null;
    }

}
