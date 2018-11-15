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

def npmRun(runTarget) {
	try{
	    sh """#!/bin/bash -e
        npm run ${runTarget}"""
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
	// archiveArtifacts 	artifacts: '**'   , onlyIfSuccessful: true
	// this.packHandler(targetBranch, targetEnv, configuration)
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

def EnvVar(configuration) {
        def var = this.json(configuration)
        for (item in var.npm_var) {
             println  "{var.prefix}"npm set {item}      
          }

    }


def packHandler(String targetBranch, String targetEnv, configuration) {
    String artifact = this.artifactName(targetBranch, targetEnv, configuration)
		try {
				sh """chmod 755 conf/package.sh
					  conf/package.sh ${artifact}"""
				dir('j2') {
      				stash name: "${artifact}", includes: artifact
      				archive artifact
			}
		} catch(error) {
			echo "FAILURE: Application Build failed"
			echo error.message
			throw error
		} finally{
			step([$class: 'WsCleanup', notFailBuild: true])
		} //TryCatchFinally
}

String artifactName(String targetBranch, String targetEnv, configuration) {
  def context = json(configuration)
  def currentVersion = getVersionFromPackageJSON()
  return "${context.application}-${targetBranch}-artifact-${currentVersion}.tar.gz"
}

def publishNexus(String targetBranch, String targetEnv, configuration){
    def currentVersion = getVersionFromPackageJSON().version
    String nexusURL = json.nexus.url ?: 'http://invalid.url/'
    String customCredentials = json.nexus.credentials ?: null
	try{
		stash 	name: "artifact-${context.application}-${targetBranch}-${currentVersion}" , includes: "**"
		archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
		echo "PUBLISH: ${this.name()} artifact  to ${nexusURL} "
		nexusPublisher {
					targetURL = nexusURL
					tarball = this.name()
				}
		} catch (error) {
 			echo "Failed to publish artifact to Nexus"
 		}
}
return this;
