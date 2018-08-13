node() {
  def scmVars = checkout scm
  def appName = "mobile-miner"
  def packageVersion = "1.0.${BUILD_NUMBER}"
  def slackChannel = "#core-dev"
  gitBranch = scmVars.GIT_BRANCH.replace('origin/', '')

  slackSend channel: "${slackChannel}", color: '#439FE0', message: "Starting ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
  currentBuild.displayName = "#${BUILD_NUMBER} - ${gitBranch}"

      stage('Checkout') {
          checkout scm
      }

      stage('Compile') {
        try {
          sh "./gradlew compileDebugSources"
        }
        catch (exc) {
          slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
          currentBuild.result = 'FAILURE'
          throw exc
        }
      }

      stage('Test') {
        try {
            sh "./gradlew testDebugUnitTest testDebugUnitTest"
            junit '**/TEST-*.xml'
          }
          catch (exc) {
            slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
            currentBuild.result = 'FAILURE'
            throw exc
          }
      }

      stage('SAST') {
          sh "./gradlew lintDebug"
          androidLint pattern: '**/lint-results-*.xml'
      }

      stage('Build') {
        try {
            sh "ENVFILE=.env.production ./gradlew assembleRelease"
            archiveArtifacts '**/*.apk'
          }
          catch (exc) {
            slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
            currentBuild.result = 'FAILURE'
            throw exc
          }
      }
      
      stage('Publish') {
        try {
          if (gitBranch == "master") {
            s3BucketName = "s3://path-apk-releases"
            assumeRole = "arn:aws:iam::217940666184:role/jenkins-assume-role"
          }
          // Configured as a Jenkins file credential 
          SIGNING_KEYSTORE = credentials('path-android-signing-keystore')
          SIGNING_KEY_PASSWORD = credentials('path-android-signing-password')
          sh """
            sh "ENVFILE=.env.production ./gradlew assembleRelease"
            set +x
            temp_role=\$(/usr/bin/aws sts assume-role \
              --role-arn "${assumeRole}" \
              --role-session-name "jenkins")
            export AWS_ACCESS_KEY_ID=\$(echo \$temp_role | jq .Credentials.AccessKeyId | xargs)
            export AWS_SECRET_ACCESS_KEY=\$(echo \$temp_role | jq .Credentials.SecretAccessKey | xargs)
            export AWS_SESSION_TOKEN=\$(echo \$temp_role | jq .Credentials.SessionToken | xargs)
            set -x
            aws s3 cp ./**/*.apk s3://${s3BucketName}/${appName}/deployment/${appName}-${packageVersion}.zip
          """            
          archiveArtifacts '**/*.apk'
          //androidApkUpload googleCredentialsId: 'google-play', apkFilesPattern: '**/*-release.apk', trackName: 'beta'
         }
         catch (exc) {
             slackSend channel: "${slackChannel}", color: '#FF0000', message: "${env.STAGE_NAME} failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${gitBranch} (<${env.JOB_URL}|Open>)"
             currentBuild.result = 'FAILURE'
             throw exc
         }
      }  
}
