#!/usr/bin/groovy
package com.jenkins.library

import com.jenkins.library.ExecuteCommand
import groovy.json.JsonSlurper

def npm(runTarget) {
		println "Executing npm " + runTarget + " ..."
	    def command = "npm " + runTarget
	    new ExecuteCommand().execute(command) 
}

def npmRun(runTarget, targetEnv) {
    String artifact = this.artifactName(targetEnv)
	def context = config()
	try{
	    def command = "npm run " + runTarget
	    println "Executing npm run " + runTarget + " ..."
	    new ExecuteCommand().execute(command)
	    
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
	  writeFile file: '/tmp/package.sh', text: libraryResource('package.sh')
	  def pack = "chmod +x /tmp/package.sh"
	  sh(returnStdout: true, script: pack)
	  sh """/tmp/package.sh ${artifact}"""
	  dir('j2') {
      stash name: "artifact-${context.application}-${targetEnv}", includes: artifact
      archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
      }
}

def runfunction() {
      writeFile file: '/tmp/functions.sh', text: libraryResource('functions')
	  def func = "chmod +x /tmp/functions.sh"
	  def sh = "./tmp/functions.sh"
	  return sh
    }


def getVersionFromPackageJSON() {
	env.WORKSPACE = pwd()
	def jfile = readFile "${env.WORKSPACE}/package.json"
	HashMap packageJson  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return packageJson.version
}

def config() {
	String configPath = "${env.WORKSPACE}/pipelines/conf/build-nodejs.yaml"
	Map configFile  = readYaml file: configPath
	return configFile
}

String artifactName(String targetEnv) {
  def context = config()
  def currentVersion = getVersionFromPackageJSON()
  return "${context.application}-${targetEnv}-artifact-${currentVersion}.tar.gz"
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
