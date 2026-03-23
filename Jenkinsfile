pipeline {
    agent any

    parameters {
        booleanParam(name: 'DISCOVERY_SERVER', defaultValue: false)
        booleanParam(name: 'CONFIG_SERVER', defaultValue: false)
        booleanParam(name: 'API_GATEWAY', defaultValue: false)
        booleanParam(name: 'ORDER_SERVICE', defaultValue: false)
        booleanParam(name: 'PAYMENT_SERVICE', defaultValue: false)
        booleanParam(name: 'INVENTORY_SERVICE', defaultValue: false)
    }

    environment {
        DOCKER_HUB_USER = 'rdutta2'
        DOCKER_HUB_CRED_ID = 'docker-hub-credentials'
        GIT_CRED_ID = 'github-cred'
        BUILD_TAG = "${env.BUILD_NUMBER}"
        GIT_REPO = 'https://github.com/RajKumarDutta/oms.git'
        BRANCH = 'main'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: "${BRANCH}",
                    credentialsId: "${GIT_CRED_ID}",
                    url: "${GIT_REPO}"
            }
        }

        stage('Detect Services') {
            steps {
                script {
                    def selected = []

                    // Manual selection
                    if (params.DISCOVERY_SERVER) selected << 'discovery-server'
                    if (params.CONFIG_SERVER) selected << 'config-server'
                    if (params.API_GATEWAY) selected << 'api-gateway'
                    if (params.ORDER_SERVICE) selected << 'order-service'
                    if (params.PAYMENT_SERVICE) selected << 'payment-service'
                    if (params.INVENTORY_SERVICE) selected << 'inventory-service'

                    if (selected.size() == 0) {
                        echo "No manual selection → auto-detecting changes..."

                        def changes = sh(
                            script: "git diff --name-only HEAD~1 HEAD || true",
                            returnStdout: true
                        ).trim().split("\n")

                        changes.each { file ->
                            if (file.startsWith('discovery-server/')) selected << 'discovery-server'
                            if (file.startsWith('config-server/')) selected << 'config-server'
                            if (file.startsWith('api-gateway/')) selected << 'api-gateway'
                            if (file.startsWith('order-service/')) selected << 'order-service'
                            if (file.startsWith('payment-service/')) selected << 'payment-service'
                            if (file.startsWith('inventory-service/')) selected << 'inventory-service'
                        }

                        selected = selected.unique()
                    }

                    if (selected.size() == 0) {
                        echo "No changes detected → building ALL services"
                        selected = [
                            'discovery-server',
                            'config-server',
                            'api-gateway',
                            'order-service',
                            'payment-service',
                            'inventory-service'
                        ]
                    }

                    env.SERVICES = selected.join(',')
                    echo "Services to build: ${env.SERVICES}"
                }
            }
        }

        stage('Build Services') {
            steps {
                echo "Skipping local Maven build; multi-stage Docker builds will handle compilation."
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CRED_ID) {
                        env.SERVICES.split(',').each { service ->
                            dir(service) {
                                def image = docker.build("${DOCKER_HUB_USER}/${service}:${BUILD_TAG}")
                                image.push()
                                image.push("latest")
                            }
                        }
                    }
                }
            }
        }

        stage('Update K8s Manifests') {
            steps {
                script {
                    env.SERVICES.split(',').each { service ->
                        sh """
                        sed -i 's|image: .*/${service}:.*|image: ${DOCKER_HUB_USER}/${service}:${BUILD_TAG}|g' k8s/${service}.yaml
                        """
                    }
                }
            }
        }

        stage('Push to GitHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${GIT_CRED_ID}",
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_PASS'
                )]) {
                    sh """
                        git config user.email "jenkins@example.com"
                        git config user.name "Jenkins CI"

                        git add k8s/*.yaml
                        git commit -m "update images to build ${BUILD_TAG}" || echo "No changes"

                        git push https://${GIT_USER}:${GIT_PASS}@github.com/RajKumarDutta/oms.git HEAD:${BRANCH}
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Done → ArgoCD will deploy automatically"
        }
        failure {
            echo "❌ Pipeline failed"
        }
        always {
            cleanWs()
        }
    }
}