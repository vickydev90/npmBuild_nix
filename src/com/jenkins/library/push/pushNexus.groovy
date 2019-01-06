package com.jenkins.library.push

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

def call(Closure body) {
 
   def context = config()
 
   body.resolveStrategy = Closure.DELEGATE_FIRST
   body.delegate = context
   body()
 
   def nexusURL = context.nexus.url
   def artifact = artifactName
   def credentialsID = context.nexus.credentials
 
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
 
return this;