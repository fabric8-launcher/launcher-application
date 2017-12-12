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

def deploy(name, namespace, releaseVersion, project, params){
  echo '3'
  stage "Deploying ${releaseVersion}"
  if (params){
      writeTemplateValuesToFile(params)
  }
  container(name: 'clients') {

    try {
        sh "oc get project ${namespace} | grep Active"
    } catch (err) {
        echo "${err}"
        sh "oc new-project ${namespace}"
    }

    def yaml = "https://raw.githubusercontent.com/fabric8-launcher/launcher-openshift-templates/master/openshift/launcher-template.yaml"

    echo "now deploying to namespace ${namespace}"
    sh """
      oc process -n ${namespace} --param-file=./values.txt -f ${yaml} | oc apply --force -n ${namespace} -f -
    """

    // add a route for t
    sh """
cat <<'EOF' | oc apply -n ${namespace} -f -
apiVersion: v1
kind: Route
metadata:
  name: launcher-backend
spec:
  to:
    kind: Service
    name: launcher-backend
    weight: 100
  wildcardPolicy: None
status: {}
EOF
"""

    sleep 10 // ok bad bad but there's a delay between DC's being applied and new pods being started.  lets find a better way to do this looking at the new DC perhaps?

    // wait until the pods are running
    waitUntil{
      try{
        sh "oc get pod -l deploymentconfig=launcher-backend -n ${namespace} | grep '1/1       Running'"
        echo "${name} pod Running for v ${releaseVersion}"
        return true
      } catch (err) {
        echo "waiting for ${name} to be ready..."
        return false
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

      input id: 'Proceed', message: "\n${message}"
  }
}

def writeTemplateValuesToFile(map){
    if (map){
        for (def p in mapToList(map)){
            echo p.key
            echo p.value
            sh "echo ${p.key}=${p.value} >> ./values.txt"
        }
    }
    map = null
}

// thanks to https://stackoverflow.com/questions/40159258/impossibility-to-iterate-over-a-map-using-groovy-within-jenkins-pipeline#
@NonCPS
def mapToList(depmap) {
    def dlist = []
    for (def entry2 in depmap) {
        dlist.add(new java.util.AbstractMap.SimpleImmutableEntry(entry2.key, entry2.value))
    }
    dlist
}
return this;
