#!/usr/bin/groovy
node{
  ws{

    stage 'checkout code'
        checkout scm

    stage 'build and unit test'
    
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'distortion-gh-test-user-pw',
        usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD']]) {

            withCredentials([[$class: 'StringBinding',
            credentialsId: 'distortion-gh-test-access-token', variable: 'GITHUB_TOKEN']]) {

                withCredentials([[$class: 'StringBinding',
                credentialsId: 'distortion-gh-test-client-id', variable: 'CATAPULT_GITHUB_APP_CLIENT_ID']]) {

                    withCredentials([[$class: 'StringBinding',
                    credentialsId: 'distortion-gh-test-client-secret', variable: 'CATAPULT_GITHUB_APP_CLIENT_SECRET']]) {

                        kubernetes.pod('buildtestpod').withImage('maven')
                        .withPrivileged(true)
                        .withSecret('jenkins-maven-settings','/root/.m2')
                        .inside {
                            try {
                                sh 'mvn -Dmaven.test.failure.ignore clean install'
                                step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
                            }
                            catch(e) {
                                currentBuild.result = 'FAILURE'
                                throw e
                            }
                            finally {
                                processStageResult()
                            }
                        }
                    }
                }
            }
        }
        
    stage 'integration test'

        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'distortion-gh-test-user-pw',
        usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD']]) {

            withCredentials([[$class: 'StringBinding', 
            credentialsId: 'distortion-gh-test-access-token', variable: 'GITHUB_TOKEN']]) {

                withCredentials([[$class: 'StringBinding', 
                credentialsId: 'distortion-gh-test-client-id', variable: 'CATAPULT_GITHUB_APP_CLIENT_ID']]) {

                    withCredentials([[$class: 'StringBinding', 
                    credentialsId: 'distortion-gh-test-client-secret', variable: 'CATAPULT_GITHUB_APP_CLIENT_SECRET']]) {
        
                        kubernetes.pod('itpod').withImage('maven')
                        .withPrivileged(true)
                        .withSecret('jenkins-maven-settings','/root/.m2')
                        .withEnvVar('CATAPULT_OPENSHIFT_API_URL',"${OPENSHIFT_MASTER_URL}")
                        .withEnvVar('CATAPULT_OPENSHIFT_CONSOLE_URL',"https://10.1.2.2:8443")
                        .inside {
                            try {
                                sh "mvn -Dmaven.test.failure.ignore clean verify -Pit"
                                step([$class: 'JUnitResultArchiver', testResults: '**/target/failsafe-reports/*.xml'])
                            }
                            catch(e) {
                                currentBuild.result = 'FAILURE'
                                throw e
                            }
                            finally {
                                processStageResult()
                            }
                        }
                    }
                }
            }
        }
    }
}

def processStageResult() {
    step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "${EMAIL}", sendToIndividuals: false])

    if (currentBuild.result != null) {
        sh "exit 0"
    }
}
