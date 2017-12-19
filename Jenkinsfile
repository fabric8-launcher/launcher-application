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
@Library('github.com/fabric8io/fabric8-pipeline-library@master')

def stagedProject
def releaseVersion
def newRelease
def name = 'launcher-backend'
def project = 'fabric8-launcher/launcher-backend'
def pipeline
def utils = new io.fabric8.Utils()
def ci = false
def cd = false

node {
  checkout scm
  readTrusted 'release.groovy'
  pipeline = load 'release.groovy'
  if (utils.isCI()) {
    ci = true
  } else if (utils.isCD()){
    namespace = 'launcher-backend-dev'
    cd = true
  }
}

if (ci){
//  mavenTemplate{
//    dockerTemplate{
      deployOpenShiftNode(openshiftConfigSecretName: 'fabric8-intcluster-config'){
        checkout scm

        // container('maven'){
        //   //input id: 'ok', message: 'ok'
        //   sh "mvn clean install"
        // }

        // container('docker'){
        //   sh "docker build -t --file Dockerfile.deploy fabric8/launcher-backend:PR-${env.BRANCH_NAME}-${env.BUILD_NUMBER} ."
        //   sh "docker push fabric8/launcher-backend:PR-${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        // }

        def params = [:]
        //params["LAUNCHER_KEYCLOAK_URL"] = 'https://sso.prod-preview.openshift.io/auth'
        params["LAUNCHER_CATALOG_REF"] = 'v15'
        params["BACKEND_IMAGE_TAG"] = 'ceeee6d'

        def namespace = "launcher-${env.BRANCH_NAME}".toLowerCase()
        pipeline.deploy(name, namespace, 'dummy', project, params)
      }
//    }
//  }
}
