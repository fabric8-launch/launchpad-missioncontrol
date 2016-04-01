#!/usr/bin/groovy
node{
  ws{
    stage 'checkout code'
    checkout scm

    stage 'build and deploy'
    kubernetes.pod('buildpod').withImage('maven')
    .withPrivileged(true)
    .withSecret('jenkins-maven-settings','/root/.m2')
    .inside {
        sh 'mvn clean deploy'
    }
  }
}
