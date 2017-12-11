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

def imagesBuiltByPipline() {
  return ['generator-backend']
}

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
    artifactIdToWatchInCentral = 'launcher-web'
    artifactExtensionToWatchInCentral = 'war'
    dockerOrganisation = 'fabric8'
    promoteToDockerRegistry = 'docker.io'
    imagesToPromoteToDockerHub = imagesBuiltByPipline()
  }
}

def updateDownstreamDependencies(stagedProject) {
  pushPomPropertyChangePR {
    propertyName = 'forge.version'
    projects = [
            'fabric8io/fabric8-platform'
    ]
    version = stagedProject[1]
  }
}


def deploy(name, namespace, releaseVersion, openshiftURL, keycloakURL, witApiURL, authApiURL){
  ws{
    stage "Deploying ${releaseVersion}"
    container(name: 'clients') {

      def yaml = "http://central.maven.org/maven2/io/fabric8/${name}/${releaseVersion}/${name}-${releaseVersion}-openshift.yml"

      echo "now deploying to namespace ${namespace}"
      sh """
        oc process -v WIT_URL=${witApiURL} -v AUTH_URL=${authApiURL} -v OPENSHIFT_API_URL=${openshiftURL} -v KEYCLOAK_SAAS_URL=${keycloakURL} -n ${namespace} -f ${yaml} | oc apply --force -n ${namespace} -f -
      """

      sleep 10 // ok bad bad but there's a delay between DC's being applied and new pods being started.  lets find a better way to do this looking at the new DC perhaps?

      // wait until the pods are running
      waitUntil{
        try{
          sh "oc get pod -l app=${name},provider=fabric8 -n ${namespace} | grep '1/1       Running'"
          echo "${name} pod Running for v ${releaseVersion}"
          return true
        } catch (err) {
          echo "waiting for ${name} to be ready..."
          return false
        }
      }
    }
  }
}

def approve(releaseVersion, project){
  stage('approve'){
    def changeAuthor = env.CHANGE_AUTHOR
      def message = """Forge Generator backend ${releaseVersion} has been deployed https://prod-preview.openshift.io

      Please check and approve production deployment ${env.JOB_URL}

      @${changeAuthor} @demo-team
      """
/*
      if (!changeAuthor){
          error "no commit author found so cannot comment on PR"
      }
      def pr = env.CHANGE_ID
      if (!pr){
          error "no pull request number found so cannot comment on PR"
      }
      
      container('clients'){
          flow.addCommentToPullRequest(message, pr, project)
      }
*/

      input id: 'Proceed', message: "\n${message}"
  }
}

def updateGeneratorTemplate(name, releaseVersion){
  container(name: 'clients') {
    def gitRepo = 'openshiftio/saas-openshiftio'
    def flow = new io.fabric8.Fabric8Commands()
    sh 'chmod 600 /root/.ssh-git/ssh-key'
    sh 'chmod 600 /root/.ssh-git/ssh-key.pub'
    sh 'chmod 700 /root/.ssh-git'

    git "git@github.com:${gitRepo}.git"

    sh "git config user.email fabric8cd@gmail.com"
    sh "git config user.name fabric8-cd"

    def uid = UUID.randomUUID().toString()
    def branch = "versionUpdate${uid}"
    sh "git checkout -b ${branch}"

    sh "curl -L -o homeless-templates/generator-backend.yaml http://central.maven.org/maven2/io/fabric8/${name}/${releaseVersion}/${name}-${releaseVersion}-openshift.yml"

    def message = "Update generator-backend template to ${releaseVersion}"
    sh "git commit -a -m \"${message}\""
    sh "git push origin ${branch}"
    def prId = flow.createPullRequest(message, gitRepo, branch)
    flow.mergePR(gitRepo, prId)
  }
}

return this;
