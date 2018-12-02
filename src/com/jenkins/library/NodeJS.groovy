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
        npm run ${runTarget}"""
        
	   } catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
	  writeFile file: '/tmp/package.sh', text: libraryResource('package.sh')
	  def pack = "/tmp/package.sh"
	  sh 'chmod(file:"/tmp/package.sh", perm:'+x', includes:"*")'
	  sh(returnStdout: true, script: pack)
	  dir('j2') {
      stash name: "artifact-${context.application}-${targetBranch}", includes: artifact
      archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
      }
}

String copyGlobalLibraryScript() {
  writeFile file: '/tmp/package.sh', text: libraryResource('package.sh')
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