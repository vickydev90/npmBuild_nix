package com.jenkins.library.envVar

class VARIABLES {
   public static def VariablesName() {
 
         def ENV = [:]
        ENV.HTTP_PROXY='http://10.113.140.187:3128'
 
   } 
    
   static void main(String[] args) {
      VariablesName();
   } 
}