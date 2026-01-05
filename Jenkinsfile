pipeline {
    agent any

    environment {
        NEXUS_HOST = '100.67.243.29'
        NEXUS_PORT = '30500'
        DOCKER_REGISTRY = "${NEXUS_HOST}:${NEXUS_PORT}"
        K8S_NAMESPACE = 'msa-platform'
        BUILDER_NAME = 'msa-multiarch-builder'
        PLATFORMS = 'linux/amd64,linux/arm64'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup Buildx') {
            steps {
                sh '''
                    # buildx 빌더 확인 및 생성
                    if ! docker buildx inspect ${BUILDER_NAME} > /dev/null 2>&1; then
                        echo "멀티 아키텍처 빌더 생성 중..."
                        docker buildx create --name ${BUILDER_NAME} --driver docker-container --use
                        docker buildx inspect --bootstrap
                    else
                        docker buildx use ${BUILDER_NAME}
                    fi
                    echo "Buildx 준비 완료"
                '''
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    // 변경된 파일 목록
                    def changes = ""
                    try {
                        changes = sh(
                            script: "git diff --name-only HEAD~1 HEAD || git diff --name-only HEAD",
                            returnStdout: true
                        ).trim()
                    } catch (Exception e) {
                        // 첫 커밋이거나 에러 시 전체 빌드
                        changes = "all"
                    }

                    echo "Changed files:\n${changes}"

                    // common-lib 변경 시 전체 빌드
                    env.BUILD_ALL = changes.contains('common-lib/') ? 'true' : 'false'

                    // 서비스별 변경 감지
                    env.BUILD_GATEWAY = changes.contains('gateway/') ? 'true' : 'false'
                    env.BUILD_AUTH = changes.contains('auth-service/') ? 'true' : 'false'
                    env.BUILD_USER = changes.contains('user-service/') ? 'true' : 'false'
                    env.BUILD_WEDDING = changes.contains('wedding-service/') ? 'true' : 'false'
                    env.BUILD_SCHEDULE = changes.contains('schedule-service/') ? 'true' : 'false'
                    env.BUILD_BOOKMARK = changes.contains('bookmark-service/') ? 'true' : 'false'
                    env.BUILD_FESTIVAL = changes.contains('festival-service/') ? 'true' : 'false'
                    env.BUILD_TRAVEL = changes.contains('travel-service/') ? 'true' : 'false'
                    env.BUILD_TICKETING = changes.contains('ticketing-service/') ? 'true' : 'false'
                    env.BUILD_BOOK = changes.contains('book-service/') ? 'true' : 'false'

                    // 전체 빌드 여부 출력
                    if (env.BUILD_ALL == 'true') {
                        echo "common-lib changed - will rebuild all services"
                    }
                }
            }
        }

        stage('Build common-lib') {
            when {
                expression { env.BUILD_ALL == 'true' }
            }
            steps {
                dir('common-lib') {
                    sh './gradlew clean build publishToMavenLocal -x test'
                }
            }
        }

        stage('Build & Deploy Services') {
            parallel {
                stage('gateway') {
                    when {
                        expression { env.BUILD_GATEWAY == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('gateway')
                        }
                    }
                }

                stage('auth-service') {
                    when {
                        expression { env.BUILD_AUTH == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('auth-service')
                        }
                    }
                }

                stage('user-service') {
                    when {
                        expression { env.BUILD_USER == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('user-service')
                        }
                    }
                }

                stage('wedding-service') {
                    when {
                        expression { env.BUILD_WEDDING == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('wedding-service')
                        }
                    }
                }

                stage('schedule-service') {
                    when {
                        expression { env.BUILD_SCHEDULE == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('schedule-service')
                        }
                    }
                }

                stage('bookmark-service') {
                    when {
                        expression { env.BUILD_BOOKMARK == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('bookmark-service')
                        }
                    }
                }

                stage('festival-service') {
                    when {
                        expression { env.BUILD_FESTIVAL == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('festival-service')
                        }
                    }
                }

                stage('travel-service') {
                    when {
                        expression { env.BUILD_TRAVEL == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('travel-service')
                        }
                    }
                }

                stage('ticketing-service') {
                    when {
                        expression { env.BUILD_TICKETING == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('ticketing-service')
                        }
                    }
                }

                stage('book-service') {
                    when {
                        expression { env.BUILD_BOOK == 'true' || env.BUILD_ALL == 'true' }
                    }
                    steps {
                        script {
                            buildAndDeploy('book-service')
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully"
        }
        failure {
            echo "Pipeline failed"
        }
    }
}

def buildAndDeploy(String serviceName) {
    dir(serviceName) {
        // Gradle Build
        sh './gradlew clean build -x test'

        // Docker buildx 멀티 아키텍처 빌드 & 푸시
        def dockerImage = "${DOCKER_REGISTRY}/${serviceName}"
        sh """
            docker buildx build \
                --platform ${PLATFORMS} \
                --tag ${dockerImage}:${BUILD_NUMBER} \
                --tag ${dockerImage}:latest \
                --push \
                .
        """

        // Deploy to K8s
        withKubeConfig([credentialsId: 'kubeconfig']) {
            sh """
                kubectl set image deployment/${serviceName} \
                    ${serviceName}=${dockerImage}:${BUILD_NUMBER} \
                    -n ${K8S_NAMESPACE}
                kubectl rollout status deployment/${serviceName} -n ${K8S_NAMESPACE} --timeout=120s
            """
        }
    }
}
