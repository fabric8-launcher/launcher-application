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
import io.fabric8.launcher.booster.catalog.LauncherConfiguration;

public interface AttributeMapKeys {
    String NAME = "name";
    String PROJECT_DIRECTORY_FILE = "projectDirectoryFile";
    String GIT_URL = "gitUrl";
    String GIT_OWNER_NAME = "gitOwnerName";
    String GIT_REPO_NAME = "gitRepositoryName";
    String GIT_ORGANISATION = "gitOrganisationName";
    String GIT_PROVIDER = "gitProvider";
    String GIT_REPOSITORY_PATTERN = "gitRepositoryPattern";
    String GIT_REPO_NAMES = "gitRepositories";
    String GIT_CLONED_REPOS = "gitClonedRepos";

    String CATALOG_GIT_REPOSITORY = LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REPOSITORY;
    String CATALOG_GIT_REF = LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF;

    Class<GitAccount> GIT_ACCOUNT = GitAccount.class;
    String NAMESPACE = "namespace";
    String SPACE = "labelSpace";
}
