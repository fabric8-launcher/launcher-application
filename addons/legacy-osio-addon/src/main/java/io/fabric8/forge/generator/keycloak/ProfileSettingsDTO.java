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
package io.fabric8.forge.generator.keycloak;

import java.util.Map;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.utils.Strings;
import org.jboss.forge.addon.ui.context.UIContext;

/**
 */
public class ProfileSettingsDTO {
    private String catalogGitRepo;
    private String catalogGitRef;

    public void updateAttributeMap(UIContext uiContext) {
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();

        if (Strings.isNotBlank(catalogGitRef)) {
            attributeMap.put(AttributeMapKeys.CATALOG_GIT_REF, catalogGitRef);
            System.out.println("====== setting " + AttributeMapKeys.CATALOG_GIT_REF + " to " + attributeMap.get(AttributeMapKeys.CATALOG_GIT_REF));
        }
        if (Strings.isNotBlank(catalogGitRepo)) {
            attributeMap.put(AttributeMapKeys.CATALOG_GIT_REPOSITORY, catalogGitRepo);
            System.out.println("====== setting " + AttributeMapKeys.CATALOG_GIT_REPOSITORY + " to " + attributeMap.get(AttributeMapKeys.CATALOG_GIT_REPOSITORY));
        }
    }

    @Override
    public String toString() {
        return "ProfileSettingsDTO{" +
                "catalogGitRepo='" + catalogGitRepo + '\'' +
                ", catalogGitRef='" + catalogGitRef + '\'' +
                '}';
    }

    public String getCatalogGitRepo() {
        return catalogGitRepo;
    }

    public void setCatalogGitRepo(String catalogGitRepo) {
        this.catalogGitRepo = catalogGitRepo;
    }

    public String getCatalogGitRef() {
        return catalogGitRef;
    }

    public void setCatalogGitRef(String catalogGitRef) {
        this.catalogGitRef = catalogGitRef;
    }
}
