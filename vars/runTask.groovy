/*import com.jenkins.library.Lib

String call() {
    script {
        Lib lib = new Lib()
        if (lib.returnTrue() == true ) {
            sh 'echo returnTrue returned true'
        }
        if (lib.returnFalse() == false ) {
            sh 'echo returnFalse returned false'
        }
    }
}*/


import com.jenkins.library.envVar

def call() {
	def s = new envVar();
	s.VariablesName()
}