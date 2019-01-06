package com.jenkins.library.push

def config() {
	String configPath = "${env.WORKSPACE}/pipelines/conf/build-nodejs.yaml"
	Map configFile  = readYaml file: configPath
	return configFile
}

def call(Closure body) {
 
   def context = config()
 
   body.resolveStrategy = Closure.DELEGATE_FIRST
   body.delegate = context
   body()
 
   def nexusURL = context.targetURL
   def artifact = context.tarball
   def credentialsID = 'nexus-uploader'
 
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