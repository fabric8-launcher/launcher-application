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

import com.google.common.base.Objects;
import io.fabric8.utils.DomHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 */
public final class DomUtils {

    private DomUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Updates the element content if its different and returns true if it was changed
     */
    public static boolean setElementText(Element element, String value) {
        String textContent = element.getTextContent();
        if (Objects.equal(value, textContent)) {
            return false;
        }
        element.setTextContent(value);
        return true;
    }

    /**
     * Returns the first child of the given element with the name or throws an exception
     */
    public static Element mandatoryFirstChild(Element element, String name) {
        Element child = DomHelper.firstChild(element, name);
        if (child == null) {
            throw new IllegalArgumentException("The element <" + element.getTagName() + "> should have at least one child called <" + name + ">");
        }
        return child;
    }

    /**
     * Returns the first child with the given name, lazily creating one if required with the
     * given text prepended before the element if the text is not null
     */
    public static Element getOrCreateChild(Element element, String name, String text) {
        Element child = DomHelper.firstChild(element, name);
        if (child == null) {
            Document doc = element.getOwnerDocument();
            if (text != null) {
                Text textNode = doc.createTextNode(text + "  ");
                element.appendChild(textNode);
            }
            child = doc.createElement(name);
            element.appendChild(child);
            if (text != null) {
                Text textNode = doc.createTextNode(text);
                element.appendChild(textNode);
            }
        }
        return child;
    }

    /**
     * Creates a new child of the given element adding the text after the new node
     */
    public static Element createChild(Element element, String name, String text) {
        Document doc = element.getOwnerDocument();
        if (text != null) {
            Text textNode = doc.createTextNode(text + "  ");
            element.appendChild(textNode);
        }
        Element child = doc.createElement(name);
        element.appendChild(child);
        if (text != null) {
            Text textNode = doc.createTextNode(text);
            element.appendChild(textNode);
        }
        return child;
    }

    /**
     * Adds the given text to the given node
     */
    public static Text addText(Element element, String text) {
        Document doc = element.getOwnerDocument();
        Text textNode = doc.createTextNode(text + "  ");
        element.appendChild(textNode);
        return textNode;
    }


}
