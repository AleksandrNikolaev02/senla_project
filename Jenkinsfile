pipeline {
    agent any

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
                            sh 'mvn clean package -pl app'
                        }
                    }
                }

                stage('Build File Service') {
                    steps {
                        dir('file_service') {
                            sh 'mvn clean package -pl file_service'
                        }
                    }
                }

                stage('Build Email Service') {
                    steps {
                        dir('email_service') {
                            sh 'mvn clean package -pl email_service'
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