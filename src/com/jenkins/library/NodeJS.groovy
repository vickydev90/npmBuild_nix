#!/usr/bin/groovy
package com.jenkins.library

import groovy.json.JsonSlurper

def npm(runTarget, configuration) {
	try{
	    def pref = "npm " + runTarget
	    println "Executing npm " + runTarget + " ..."
	    def command = pref.execute()
	    command.waitForOrKill( 10000 )
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
}

def npmRun(runTarget) {
	try{
	    def pref = "npm run " + runTarget
	    println "Executing npm run " + runTarget + " ..."
	    command = pref.execute()
	    command.waitForOrKill( 10000 )
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
}


def readJson(text) {
    def response = new groovy.json.JsonSlurperClassic().parseText(text)
    jsonSlurper = null
    echo "response:$response"
    return response
}

def getVersionFromPackageJSON() {
    dir(".") {
        def packageJson = readJSON file: 'package.json'
        return packageJson.version
    }
}


def json(configuration) {
	env.WORKSPACE = pwd() + configuration
	def jfile = readFile "${env.WORKSPACE}"
	HashMap configFile  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return configFile
}


def publishNexus(String targetBranch, config){
    def currentVersion = getVersionFromPackageJSON()
    String nexusURL = config.nexus.url ?: 'http://invalid.url/'
    String customCredentials = config.nexus.credentials ?: null
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
