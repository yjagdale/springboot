class AutomationSuiteExecutor {
    static def runAutomation(env) {
        runAutomation:
        {
            script {
                def mvnHome = tool 'Maven 3.3.9'
                sh "'${mvnHome}/bin/mvn' -DskiptTest=false -Dbuild.number=${targetVersion} clean package"
                sh "echo 'Executing " + env + "'"
            }
        }
    }
}