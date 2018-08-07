class RunBuild {
    static BuildApplication(boolean withUnitTest) {
        buildApplication:
        {
            script {
                echo 'Pulling...' + env.BRANCH_NAME
                def mvnHome = tool 'Maven 3.3.9'
                if (isUnix()) {
                    def targetVersion = getDevVersion()
                    print 'target build version...'
                    print targetVersion
                    sh "'${mvnHome}/bin/mvn' -DskiptTest=" + withUnitTest + " -Dbuild.number=${targetVersion} clean package"
                    def pom = readMavenPom file: 'pom.xml'
                    // get the current development version
                    developmentArtifactVersion = "${pom.version}-${targetVersion}"
                    print pom.version
                    if (withUnitTest) {
                        // execute the unit testing and collect the reports
                        junit '**//*target/surefire-reports/TEST-*.xml'
                    }
                    archiveArtifacts 'target*//*.jar'
                }
            }
        }
    }
}