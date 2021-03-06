package com.jenkins.library
// import com.jenkins.library.push.pushNexus
import groovy.json.JsonSlurper
import com.jenkins.library.envVar

def env() {
    def variables = new envVar()
    variables.VariablesName()
}



def npm(runTarget) {
	try{
		env()
		sh """#!/bin/bash -e
		npm ${runTarget}"""
	} catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	}
}

def npmRun(runTarget, targetEnv) {
	String artifact = this.artifactName(targetEnv)
	def context = config()
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
      stash name: "artifact-${context.application}-${targetEnv}", includes: artifact
      archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
      }
}

def runfunction() {
      writeFile file: '/tmp/functions.sh', text: libraryResource('functions')
	  def func = "chmod +x /tmp/functions.sh"
	  sh(returnStdout: true, script: func)
	  def sh = sh """/tmp/functions.sh"""
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


def publishNexus(targetEnv) {
  if (targetEnv == "integration-branch") {
  String artifact = this.artifactName(String env)
  def context = config()
  //withCredentials([usernamePassword(credentialsId: 'nexusLocal', passwordVariable: 'pass', usernameVariable: 'test')]){
  def nexusURL = context.nexus.url
  def credentialsID = context.nexus.credentials
  def packageVersion = getVersionFromPackageJSON()
  
  try {
    dir('j2') {
      deleteDir()
      unstash "artifact-${context.application}-${targetEnv}"
      artifact = sh(returnStdout: true, script: 'ls *.tar.gz | head -1').trim()
      withCredentials([
       usernameColonPassword(    credentialsId: credentialsID,
       variable: 'NEXUS_CREDS')
   ]) { sh     """curl --insecure    -sS \
                           -u $NEXUS_CREDS \
                           --upload-file \
                           ${artifact} \
                           ${nexusURL}/${artifact} 
               """  
   }
      
        }
  } catch (Exception ex) {
		println "FAILED: export ${ex.message}"
		throw ex
	} finally {
		// step([$class: 'WsCleanup', notFailBuild: true])
  		}
  	}
  }