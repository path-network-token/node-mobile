node() {
 def scmVars = checkout scm
 def appName = "mobile-miner"
 def packageVersion = "1.0.${BUILD_NUMBER}"
 def slackChannel = "#core-dev"
 def gitCommitMsg = sh(script: 'git show -s --format=%B --oneline HEAD', returnStdout: true).trim()

 gitBranch = scmVars.GIT_BRANCH.replace('origin/', '')

 slackSend channel: "${slackChannel}", color: '#439FE0', message: "Starting ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitCommitMsg} (<${env.JOB_URL}|Open>)"
 currentBuild.displayName = "#${BUILD_NUMBER} - ${gitBranch}"

 /*
  @TODO Test stage: Need if/else logic based on debug/release
  ./gradlew testDebugUnitTest testDebugUnitTest"
  ./gradlew testReleaseUnitTest --stacktrace

  @TODO Test stage: Need to find where JUnit tests are being stored 
  IE: /build/test-results/testDebugUnitTest"

  @TODO Static Analysis stage:Need if/else logic for this too. 
  ./gradlew lintDebug
  androidLint pattern: 'build/lint-results.xml'
  
  @TODO Static Analysis stage: Can enforce restrictions, IE To mark the build as unstable if there
  are more than 10 Lint warnings, and to mark the build as failed
  if there are more than 30 Lint warnings:
  androidLint unstableTotalAll: '10', failedTotalAll: '30'
  Info: https://wiki.jenkins.io/display/JENKINS/Android+Lint+Plugin
 */ 

      stage('Checkout') {
         checkout scm
         sh 'npm install'
     }

     stage('Environment Preparation') {
       docker.image('153323634045.dkr.ecr.us-west-2.amazonaws.com/android-sdk-node:latest').inside("-e HOME=/tmp") {
         stage('Prepare environment') {
           try {
             println 'Preparing environment...'
             sh """
             cd android 
             ./gradlew compileDebugSources
             """
           } catch (exc) {
              slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
              currentBuild.result = 'FAILURE'
              throw exc
          }
         }
         stage('Test') {
           try {
           sh "cd android;./gradlew testDebugUnitTest testDebugUnitTest"
           //junit '**/TEST-*.xml'
           } catch (exc) {
              slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
              currentBuild.result = 'FAILURE'
              throw exc
          }
       }
       /*stage('Static Analysis') {
         try {
           sh "cd android;./gradlew lintDebug"
           androidLint pattern: 'android/app/build/reports/lint-results-debug.xml' 
         } catch (exc) {
           //androidLint pattern: 'android/app/build/reports/lint-results-debug.xml' 
           currentBuild.result = 'FAILURE'
           throw exc   
         }
       }*/
       stage('Build') {
         try {
           sh "cd android;ENVFILE=.env.production ./gradlew assembleRelease"
           archiveArtifacts './android/app/outputs/*.apk'
           } catch (exc) {
              slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
              currentBuild.result = 'FAILURE'
              throw exc
          }
       }
     }
   }
   stage('Publish') {
     try {
       if (gitBranch == "develop") {
         s3BucketName = "s3://path-apk-releases"
         assumeRole = "arn:aws:iam::217940666184:role/jenkins-assume-role"
       }
       // Configured as a Jenkins file credential and secret value
       SIGNING_KEYSTORE = credentials('path-android-signing-keystore')
       SIGNING_KEY_PASSWORD = credentials('path-android-signing-password')
       sh """
         set +x
         temp_role=\$(/usr/bin/aws sts assume-role \
           --role-arn "${assumeRole}" \
           --role-session-name "jenkins")
         export AWS_ACCESS_KEY_ID=\$(echo \$temp_role | jq .Credentials.AccessKeyId | xargs)
         export AWS_SECRET_ACCESS_KEY=\$(echo \$temp_role | jq .Credentials.SecretAccessKey | xargs)
         export AWS_SESSION_TOKEN=\$(echo \$temp_role | jq .Credentials.SessionToken | xargs)
         set -x
         aws s3 cp ./android/app/outputs/*.apk s3://${s3BucketName}/${appName}/${appName}-${packageVersion}.apk
       """            
       //archiveArtifacts './android/app/outputs/*.apk'
       //androidApkUpload googleCredentialsId: 'google-play', apkFilesPattern: '**/*-release.apk', trackName: 'beta'
       } catch (exc) {
          slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
          currentBuild.result = 'FAILURE'
          throw exc
      }
   }
}

