
pipeline {
    // run on jenkins nodes tha has java 8 label
    agent any

    environment {
        EMAIL_RECIPIENTS = 'jagdale0210@gmail.com'
    }

    stages {
        stage('Build with unit testing') {
            GroovyClassLoader gcl = new GroovyClassLoader();
            File f = new File("RunBuild.groovy")
            Class runBuild = gcl.parseClass(f)
            runBuild.BuildApplication(true)
        }
    }
}