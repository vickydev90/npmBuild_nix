#!/usr/bin/groovy
package com.jenkins.library

import groovy.json.JsonSlurper

def npm(runTarget) {
   def pref = value().prefix
   sh """#!/bin/bash -e
        ${pref}
	npm ${runTarget}"""
}

def npmRun(runTarget) {
    sh """#!/bin/bash -e
	export PATH=/usr/local/bin:$PATH
        npm run ${runTarget}"""
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


def value() {
	def config = libraryResource 'config.json'
	writeFile file: '/tmp/config.json', text: config
	sh """ chmod 755 /tmp/config.json """
	def inputFile = new File("/tmp/config.json")
	def InputJSON = new JsonSlurper().parseText(inputFile.text)
	return InputJSON
}

def json(configuration) {
	env.WORKSPACE = pwd()
	def jfile = readFile "${env.WORKSPACE}/config.json"
	HashMap configurationFile  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()

	println configurationFile.prefix
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
