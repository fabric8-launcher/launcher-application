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
package io.fabric8.forge.generator;

import io.fabric8.forge.generator.git.GitAccount;

/**
 */
public class AttributeMapKeys {
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String PROJECT_DIRECTORY_FILE = "projectDirectoryFile";
    public static final String GIT_URL = "gitUrl";
    public static final String GIT_OWNER_NAME = "gitOwnerName";
    public static final String GIT_REPO_NAME = "gitRepositoryName";
    public static final String GIT_ORGANISATION = "gitOrganisationName";
    public static final String GIT_PROVIDER = "gitProvider";
    public static final String GIT_REPOSITORY_PATTERN = "gitRepositoryPattern";
    public static final String GIT_REPO_NAMES = "gitRepositories";
    public static final String GIT_CLONED_REPOS = "gitClonedRepos";

    public static final String CATALOG_GIT_REPOSITORY = "LAUNCHPAD_BACKEND_CATALOG_GIT_REPOSITORY";
    public static final String CATALOG_GIT_REF = "LAUNCHPAD_BACKEND_CATALOG_GIT_REF";

    public static final Class<GitAccount> GIT_ACCOUNT = GitAccount.class;
    public static final String NAMESPACE = "namespace";
    public static final String SPACE = "labelSpace";

}
