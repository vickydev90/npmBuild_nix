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
	stash	name: 'testpackage', includes: '**'
	archiveArtifacts 	artifacts: testpackage , onlyIfSuccessful: true
}


def getVersionFromPackageJSON() {
	env.WORKSPACE = pwd()
	def jfile = readFile "${env.WORKSPACE}/package.json"
	HashMap packageJson  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return packageJson
}

def json(configuration) {
	env.WORKSPACE = pwd() + configuration
	def jfile = readFile "${env.WORKSPACE}"
	HashMap configFile  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return configFile
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
