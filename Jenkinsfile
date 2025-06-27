pipeline {
    agent any
    tools {
        maven 'maven3.6.3'
    }

    stages {
        stage('Cloning') {
            steps {
                git url: 'https://github.com/AleksandrNikolaev02/senla_project.git',
                                    branch: 'main'
            }
        }

        stage('Build Common DTO') {
            steps {
                dir('common-dto') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Build modules') {
            parallel {
                stage('Build App') {
                    steps {
                        dir('app') {
                            sh 'mvn clean package'
                        }
                    }
                }

                stage('Build File Service') {
                    steps {
                        dir('file_service') {
                            sh 'mvn clean package'
                        }
                    }
                }

                stage('Build Email Service') {
                    steps {
                        dir('email_service') {
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }

    }

    post {
        success {
            echo 'Build and Deploy succeeded!'
        }
        failure {
            echo 'Build or Deploy failed!'
        }
    }
}