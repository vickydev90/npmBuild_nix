#!/usr/bin/groovy

package com.jenkins.library

import groovy.json.JsonSlurper



def npm(runTarget) {
	try{
		runfunction()
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
	 	runfunction()
	    sh """#!/bin/bash -e
        npm run ${runTarget}"""
        
	   } catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
	  writeFile file: '/tmp/package.sh', text: libraryResource('package.sh')
	  def pack = "chmod +x /tmp/package.sh"
	  sh(returnStdout: true, script: pack)
	  sh """/tmp/package.sh ${artifact}"""
	  dir('j2') {
      stash name: "artifact-${context.application}-${targetBranch}", includes: artifact
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

def json(configuration) {
	env.WORKSPACE = pwd() + configuration
	def jfile = readFile "${env.WORKSPACE}"
	HashMap configFile  = (new HashMap(new groovy.json.JsonSlurperClassic().parseText(jfile))).asImmutable()
	return configFile
}


String artifactName(String targetBranch, String targetEnv, configuration) {
  def context = json(configuration)
  def currentVersion = getVersionFromPackageJSON()
  return "${context.application}-${targetBranch}-artifact-${currentVersion}.tar.gz"
}
            

def publishNexus(targetBranch, targetEnv, configuration) {
  if (targetEnv == "integration") {
  String artifact = this.artifactName(targetBranch, targetEnv, configuration)
  withCredentials([usernamePassword(credentialsId: 'nexusLocal', passwordVariable: 'pass', usernameVariable: 'test')]){
  def packageVersion = getVersionFromPackageJSON()
  def context = json(configuration)
  //echo "PUBLISH: ${this.name()} artifact version: ${packageVersion} "
  try {
    dir('j2') {
      deleteDir()
      unstash "artifact-${context.application}-${targetBranch}"
      artifact = sh(returnStdout: true, script: 'ls *.tar.gz | head -1').trim()
      nexusArtifactUploader artifacts: [[artifactId: ${context.application}, classifier: '', file: artifact, type: 'tar.gz']], credentialsId: 'nexusLocal', groupId: 'com.llyodsbanking.nodejs', nexusUrl: ${context.nexus.url}, nexusVersion: 'nexus2', protocol: 'http', repository: 'releases', version: 'packageVersion'
        }
  } catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	} finally {
  		}
  	}
  	}
  }