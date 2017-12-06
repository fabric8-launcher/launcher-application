#!/usr/bin/groovy
/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
def stage(){
    return stageProject{
        project = 'fabric8-launcher/launcher-backend'
        useGitTagForNextVersion = true
    }
}

def release(project){
    releaseProject{
        stagedProject = project
        useGitTagForNextVersion = true
        helmPush = false
        groupId = 'io.fabric8.launcher'
        githubOrganisation = 'fabric8-launcher'
        artifactIdToWatchInCentral = 'launcher-parent'
        artifactExtensionToWatchInCentral = 'jar'
    }
}

def mergePullRequest(prId){
    mergeAndWaitForPullRequest{
        project = 'fabric8-launcher/launcher-backend'
        pullRequestId = prId
    }
}

return this;