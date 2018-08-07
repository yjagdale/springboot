pipeline {
    // run on jenkins nodes tha has java 8 label
    agent any
    // global env variables
    environment {
        EMAIL_RECIPIENTS = 'jagdale0210@gmail.com'
    }
    stages {

        stage('Build with unit testing') {
            steps {
                parallel {
                  stage ('clean project') {
                      script {
                          // Get the Maven tool.
                          // ** NOTE: This 'M3' Maven tool must be configured
                          // **       in the global configuration.
                          echo 'Pulling...' + env.BRANCH_NAME
                          def mvnHome = tool 'Maven 3.3.9'
                          if (isUnix()) {
                              def targetVersion = getDevVersion()
                              print 'target build version...'
                              print targetVersion
                              sh "'${mvnHome}/bin/mvn' -DskiptTest=false -Dbuild.number=${targetVersion} clean package"
                              def pom = readMavenPom file: 'pom.xml'
                              // get the current development version
                              developmentArtifactVersion = "${pom.version}-${targetVersion}"
                              print pom.version
                              // execute the unit testing and collect the reports
                              junit '**//*target/surefire-reports/TEST-*.xml'
                              archiveArtifacts 'target*//*.jar'
                          } else {
                              bat(/"${mvnHome}\bin\mvn" -Dintegration-tests.skip=true clean package/)
                              def pom = readMavenPom file: 'pom.xml'
                              print pom.version
                              junit '**//*target/surefire-reports/TEST-*.xml'
                              archive 'target*//*.jar'
                          }
                      }
                  }
                    stage ('Run print statements') {
                        sh "echo 'waiting for completiong'; sleep 60;"
                    }
                }
            }
        }
    }
    post {
        // Always runs. And it runs before any of the other post conditions.
        always {
            script {
                print "always"
            }
        }
        success {
            sendEmail("Successful")
            build 'GaugeAutomation'
        }
        unstable {
            sendEmail("Unstable")
        }
        failure {
            sendEmail("Failed")
        }
    }
    // The options directive is for configuration that applies to the whole job.
    options {
        // For example, we'd like to make sure we only keep 10 builds at a time, so
        // we don't fill up our storage!
        buildDiscarder(logRotator(numToKeepStr: '3'))
        // And we'd really like to be sure that this build doesn't hang forever, so
        // let's time it out after an hour.
        timeout(time: 25, unit: 'MINUTES')
    }
}
// get change log to be send over the mail
@NonCPS
def getChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ""
    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += " - ${truncated_msg} [${entry.author}]\n"
        }
    }
    if (!changeString) {
        changeString = " - No new changes"
    }
    return changeString
}

def sendEmail(status) {
    /*  mail(
              to: "$EMAIL_RECIPIENTS",
              subject: "Build $BUILD_NUMBER - " + status + " (${currentBuild.fullDisplayName})",
              body: "Changes:\n " + getChangeString() + "\n\n Check console output at: $BUILD_URL/console" + "\n")*/
    print status
}

def getDevVersion() {
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber
    if (gitCommit == null) {
        versionNumber = env.BUILD_NUMBER
    } else {
        versionNumber = gitCommit.take(8)
    }
    print 'build  versions...'
    print versionNumber
    return versionNumber
}

def getReleaseVersion() {
    def pom = readMavenPom file: 'pom.xml'
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber
    if (gitCommit == null) {
        versionNumber = env.BUILD_NUMBER
    } else {
        versionNumber = gitCommit.take(8)
    }
    return pom.version.replace("-SNAPSHOT", ".${versionNumber}")
}
