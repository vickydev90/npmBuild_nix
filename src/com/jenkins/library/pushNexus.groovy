package com.jenkins.library
import com.jenkins.library.NodeJS

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