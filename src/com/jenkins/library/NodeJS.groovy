#!/usr/bin/groovy

package com.jenkins.library

import groovy.json.JsonSlurper

def npm(runTarget, configuration) {
	try{
		sh """#!/bin/bash -e
		 npm ${runTarget}"""
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
}

def npmRun(runTarget, targetBranch, targetEnv, configuration) {
	String artifact = this.artifactName(targetBranch, targetEnv, configuration)
	def context = json(configuration)
	try{
	    sh """#!/bin/bash -e
        npm run ${runTarget}
        chmod 755 conf/package.sh
		conf/package.sh ${artifact}"""
		dir('j2') {
      stash name: "artifact-${context.application}-${targetBranch}", includes: artifact
      archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
      }
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
	// archiveArtifacts 	artifacts: '**'   , onlyIfSuccessful: true
}


def getVersionFromPackageJSON() {
	env.WORKSPACE = pwd()
	def jfile = readFile "${env.WORKSPACE}/package.json"
	HashMap packageJson  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return packageJson.version
}

def json(configuration) {
	env.WORKSPACE = pwd() + configuration
	def jfile = readFile "${env.WORKSPACE}"
	HashMap configFile  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return configFile
}

//def EnvVar(configuration) {
  //      def var = this.json(configuration)
    //    for (item in var.npm_var) {
      //       sh """ ${var.prefix}npm set """ ${item}   
        //  }

    //}




String artifactName(String targetBranch, String targetEnv, configuration) {
  def context = json(configuration)
  def currentVersion = getVersionFromPackageJSON()
  return "${context.application}-${targetBranch}-artifact-${currentVersion}.tar.gz"
}

def publishNexus(targetBranch, targetEnv, configuration) {
  if (targetEnv == "integration") {
  String artifact
  def packageVersion = getVersionFromPackageJSON()
  def context = json(configuration)
  //echo "PUBLISH: ${this.name()} artifact version: ${packageVersion} "
  try {
    dir('j2') {
      deleteDir()
      unstash "artifact-${context.application}-${targetBranch}"
      artifact = sh(returnStdout: true, script: 'ls *.tar.gz | head -1').trim()
      nexusArtifactUploader {
      	credentialsId: ''
    	groupId: 'content.repositories'
    	nexusUrl: 'localhost:8081/nexus'
    	nexusVersion: 'nexus2'
    	protocol: 'http'
    	repository: 'Releases'
    	version: '2.4'
    	artifact {
            artifactId('releases')
            type('*.tar.gz')
            classifier('debug')
            file('*.tar.gz')
        }
      }
    }
  } catch (error ex) {
    echo "Failed to publish artifact to Nexus"
    throw ex
  } finally {
  }
}
}

return this;
