class Utilities{
    static  def getDevVersion() {
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

    static  def getReleaseVersion() {
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


    static  def runBuild() {
        build:
        {
            sh "echo 'Executing test'"
        }
    }

    static  def runAutomation(env) {
        runAutomation:
        {
            sh "echo 'Executing " + env + "'"
        }
    }
}