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
def name = 'generator-backend'
def project = 'fabric8io/generator-backend'
def pipeline
def utils = new io.fabric8.Utils()


node{
  properties([
    parameters ([
            choice(choices: 'new release\nredeploy latest', description: 'Optionally avoid a new release and redeploy the latest available version?', name: 'release')
      ])
  ])
  newRelease = params.release == 'new release' ? true : false
}

if (utils.isCI()){
  
  echo 'CI not enabled'

} else if (utils.isCD()){

  if (newRelease){
    releaseNode{
      ws{
        checkout scm
        readTrusted 'release.groovy'

        sh "git remote set-url origin git@github.com:fabric8-launcher/launcher-backend.git"

        pipeline = load 'release.groovy'

        stage('Stage') {
          stagedProject = pipeline.stage()
          releaseVersion = stagedProject[1]
        }

        stage('Promote') {
          pipeline.release(stagedProject)
        }

        stage('Update downstream dependencies') {
          pipeline.updateDownstreamDependencies(stagedProject)
        }
      }
    }
  } else {
    node {
      def cmd = "curl -L http://central.maven.org/maven2/io/fabric8/${name}/maven-metadata.xml | grep '<latest' | cut -f2 -d'>'|cut -f1 -d'<'"
      releaseVersion = sh(script: cmd, returnStdout: true).toString().trim()
      echo "Skipping release and redeploying ${releaseVersion}"
    }
  }


  deployOpenShiftNode(openshiftConfigSecretName: 'dsaas-preview-config', label: "deploy_prodpreview_generator_backend_master_${env.BUILD_NUMBER}"){
    def namespace = 'dsaas-preview'
    def witApiURL = 'https://api.prod-preview.openshift.io/'
    def authApiURL = 'https://auth.prod-preview.openshift.io'
    def openshiftURL = 'https://api.free-int.openshift.com'
    def keycloakURL = 'https://sso.prod-preview.openshift.io'

    if (!pipeline){
        checkout scm
        pipeline = load 'release.groovy'
    }
    
    pipeline.deploy(name, namespace, releaseVersion, openshiftURL, keycloakURL, witApiURL, authApiURL)
  }

/*
    pipeline.approve(releaseVersion, project)
    if (newRelease){
      pipeline.updateGeneratorTemplate(name, releaseVersion)
    }
  }

  deployOpenShiftNode(openshiftConfigSecretName: 'dsaas-prod-config', label: "deploy_prod_generator_backend_master_${env.BUILD_NUMBER}"){
    def namespace = 'dsaas-production'
    def witApiURL = 'https://api.openshift.io/'
    def authApiURL = 'https://auth.openshift.io'
    def openshiftURL = 'https://api.starter-us-east-2.openshift.com'
    def keycloakURL = 'https://sso.openshift.io'
    pipeline.deploy(name, namespace, releaseVersion, openshiftURL, keycloakURL, witApiURL, authApiURL)

  }
*/
}
