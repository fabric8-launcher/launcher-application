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
package io.fabric8.forge.generator.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TenantAttributesDTO extends DtoSupport {
    private String id;
    private String type;
    private String email;
    private Date createdAt;
    private List<NamespaceDTO> namespaces = new ArrayList<>();

    @Override
    public String toString() {
        return "TenantAttributesDTO{" +
                "id='" + id + '\'' +
                ", namespaces=" + namespaces +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<NamespaceDTO> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<NamespaceDTO> namespaces) {
        this.namespaces = namespaces;
    }
}
