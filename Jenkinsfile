pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = 'YOUR_DOCKERHUB_USERNAME' // Change this
        DOCKER_HUB_CRED_ID = 'docker-hub-credentials' // Jenkins Credential ID
        BUILD_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Services') {
            parallel {
                stage('Discovery Server') {
                    steps {
                        dir('discovery-server') {
                            sh 'mvn clean install -DskipTests'
                        }
                    }
                }
                stage('Config Server') {
                    steps {
                        dir('config-server') {
                            sh 'mvn clean install -DskipTests'
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean install -DskipTests'
                        }
                    }
                }
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn clean install -DskipTests'
                        }
                    }
                }
                stage('Payment Service') {
                    steps {
                        dir('payment-service') {
                            sh 'mvn clean install -DskipTests'
                        }
                    }
                }
                stage('Inventory Service') {
                    steps {
                        dir('inventory-service') {
                            sh 'mvn clean install -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    def services = ['discovery-server', 'config-server', 'api-gateway', 'order-service', 'payment-service', 'inventory-service']
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_HUB_CRED_ID}") {
                        services.each { service ->
                            dir(service) {
                                def customImage = docker.build("${DOCKER_HUB_USER}/${service}:${BUILD_TAG}")
                                customImage.push()
                                customImage.push('latest')
                            }
                        }
                    }
                }
            }
        }

        stage('Update GitOps Manifests') {
            steps {
                script {
                    // This updates the image tags in your K8s YAML files
                    sh """
                        sed -i 's|image: .*/discovery-server:.*|image: ${DOCKER_HUB_USER}/discovery-server:${BUILD_TAG}|g' k8s/discovery-server.yaml
                        sed -i 's|image: .*/config-server:.*|image: ${DOCKER_HUB_USER}/config-server:${BUILD_TAG}|g' k8s/config-server.yaml
                        sed -i 's|image: .*/api-gateway:.*|image: ${DOCKER_HUB_USER}/api-gateway:${BUILD_TAG}|g' k8s/api-gateway.yaml
                        sed -i 's|image: .*/order-service:.*|image: ${DOCKER_HUB_USER}/order-service:${BUILD_TAG}|g' k8s/order-service.yaml
                        sed -i 's|image: .*/payment-service:.*|image: ${DOCKER_HUB_USER}/payment-service:${BUILD_TAG}|g' k8s/payment-service.yaml
                        sed -i 's|image: .*/inventory-service:.*|image: ${DOCKER_HUB_USER}/inventory-service:${BUILD_TAG}|g' k8s/inventory-service.yaml
                    """
                }
            }
        }
        
        stage('Push Changes to Git') {
            steps {
                // IMPORTANT: Ensure Jenkins has git credentials to push back to the repo
                sh """
                    git config user.email "jenkins@example.com"
                    git config user.name "Jenkins CI"
                    git add k8s/*.yaml
                    git commit -m "chore: update image tags to build ${BUILD_TAG}"
                    git push origin head
                """
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
