#!/usr/bin/groovy

import settings

def call(settings set) 
{
    switch (set.flow) {
        case "feature-CI":
            featureCIFlow(set)
            break
        case "master-CI":
            masterCIFlow(set)
            break
        case "master-Release":
            masterReleaseFlow(set)
            break
        default:
            echo "[ERROR] '" + flow + "' flow - not supported!";
            currentBuild.result = 'FAILURE'
            break
    }    
}

def featureCIFlow(settings set)
{
    echo "[Flow] " + flow;
    gitUpdate(set.gitUrl)
    mavenBuild()
    mavenUnitTests()
    ciPostStep()
}

def masterCIFlow(settings set)
{
    echo "[Flow] " + flow;
    gitUpdate(set.gitUrl)
    mavenBuild()
    mavenUnitTests()
    ciPostStep("target/**/*.tar")
}

def masterReleaseFlow(settings set)
{
    echo "[Flow] " + flow;
    gitUpdate(set.gitUrl)
    mavenBuild()
    mavenUnitTests()
    ciPostStep("target/**/*.tar")
}





def gitUpdate(String gitURL, String gitBranch = "master", String phaseTitle = "GIT checkout")
{
    stage( phaseTitle ) {
        echo "[GIT URL] " + gitURL ;
        echo "[GIT BRANCH] " + gitBranch;
    }
}

def mavenBuild(String mavenPomPath = "pom.xml", String mavenGoals = "clean compile -DskipTests", String phaseTitle = "Maven build")
{
    stage( phaseTitle ) {
        echo "[Maven POM path] " + mavenPomPath;
        echo "[MAVEN goals] " + mavenGoals;
    }
}

def mavenUnitTests(String mavenPomPath = "pom.xml", String mavenGoals = "test", String phaseTitle = "Unit Tests")
{
    stage( phaseTitle ) {
        echo "[Maven POM path] " + mavenPomPath;
        echo "[MAVEN goals] " + mavenGoals;
    }
}

def ciPostStep(String archiveFileSet = "target/**/*.jar", String junitFiles = "target/surefire-reports/*.xml", String phaseTitle = "Post-build")
{
    stage( phaseTitle ) {
        archiveFiles(archiveFileSet)
        junitReport(junitFiles)
        notifyBuild('SUCCESS')
    }
}

def archiveFiles(String archiveFileSet = "target/**/*.jar")
{
    echo "[Archive] " + archiveFileSet;
    // archive archiveFileSet
}

def junitReport(String junitFiles = "target/surefire-reports/*.xml")
{
    echo "[Junit files] " + junitFiles;
    // junit junitFiles
}

def notifyBuild(String buildStatus = 'STARTED') {
    // build status of null means successful
    buildStatus = buildStatus ?: 'SUCCESS'
    // Default values
    def colorName = 'RED'
    def colorCode = '#FF0000'
    def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
    def summary = "${subject} (${env.BUILD_URL})"
    def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p><p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""
    // Override default values based on build status
    if (buildStatus == 'STARTED') {
        color = 'YELLOW'
        colorCode = '#FFFF00'
    } else if (buildStatus == 'SUCCESS') {
        color = 'GREEN'
        colorCode = '#00FF00'
    } else {
        color = 'RED'
        colorCode = '#FF0000'
    }
    // Send notifications
    emailext (
        subject: summary,
        body: details,
        mimeType: 'text/html',
        to: "${env.DEFAULT_RECIPIENTS}",
        recipientProviders: [[$class: 'DevelopersRecipientProvider'],[$class: 'CulpritsRecipientProvider']]
    )
}
