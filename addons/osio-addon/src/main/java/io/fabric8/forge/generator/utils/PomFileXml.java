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
import java.io.FileNotFoundException;

import javax.xml.transform.TransformerException;

import io.fabric8.utils.DomHelper;
import org.w3c.dom.Document;

/**
 */
public class PomFileXml {
    private final File file;
    private final Document document;

    public PomFileXml(File file, Document document) {
        this.file = file;
        this.document = document;
    }

    public File getFile() {
        return file;
    }

    public Document getDocument() {
        return document;
    }

    public PomFileXml updateDocument(Document document) throws FileNotFoundException, TransformerException {
        DomHelper.save(document, file);
        return new PomFileXml(file, document);
    }
}
