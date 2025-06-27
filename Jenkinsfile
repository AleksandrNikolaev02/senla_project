pipeline {
    agent any

    stages {
        stage('Cloning') {
            steps {
                git url: 'https://github.com/AleksandrNikolaev02/senla_project.git',
                                    branch: 'main'
            }
        }

        stage('Build library') {
            steps {
                sh 'cd ./common-dto/'
                sh 'mvn clean install'
            }
        }

        stage('Build modules') {
            parallel {
                stage('Build app') {
                    steps {
                        sh 'cd ./app/'
                        sh 'mvn clean package'
                    }
                }
                stage('Build email_service') {
                    steps {
                        sh 'cd ./email_service/'
                        sh 'mvn clean package'
                    }
                }
                stage('Build file_service') {
                    steps {
                        sh 'cd ./file_service/'
                        sh 'mvn clean package'
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