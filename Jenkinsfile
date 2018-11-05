pipeline
{
  agent { node { label 'master'}}

    stages {
        stage('Test the sharedLib') {
            steps{
                sh "./gradlew test"
            }
        }
        stage('Run Lint on the sharedLib') {
            steps{
                sh "./gradlew check"
            }
        }
        stage('Run Clover code coverage'){
            steps{
                sh "./gradlew cloverGenerateReport"
            }
        }
    }

    post {
        success {
            // publish unit-test report
            publishHTML target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'build/reports/tests/test/',
                reportFiles: 'index.html',
                reportName: 'Unit Test Report'
            ]

            // publish Linting report
            publishHTML target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'build/reports/codenarc/',
                reportFiles: 'main.html',
                reportName: 'Codenarc Lint Report'
            ]

            // publish Code coverage
            step([
                $class              : "CloverPublisher",
                cloverReportDir     : "build/reports/clover",
                cloverReportFileName: "clover.xml"
            ])
            
            // Generate groovydoc
            sh "./gradlew groovydoc"

            // Publish groovydoc
            publishHTML target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'build/docs/groovydoc',
                reportFiles: 'index.html',
                reportName: 'Generated GroovyDoc'
            ]
        }
    }
}