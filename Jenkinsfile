pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git credentialsId: 'github-https-token',
                    url: 'https://github.com/conyviera/ProyectoTingeso.git',
                    branch: 'main'
            }
        }

        stage('Test backend') {
            steps {
                dir('Backend') {
                    sh '''
                        echo "=== DEBUG RUTA ==="
                        pwd
                        ls -l

                        echo "=== Dando permisos de ejecución a mvnw ==="
                        chmod +x mvnw

                        echo "=== Ejecutando tests con mvnw ==="
                        ./mvnw -B clean test
                    '''
                }
            }
        }

        stage('Build backend image') {
            steps {
                dir('Backend') {
                    sh '''
                        docker build -t conyviera/proyectotingeso-backend:latest .
                    '''
                }
            }
        }

        stage('Build frontend image') {
            steps {
                dir('Frontend') {
                    sh '''
                        docker build \
                          --build-arg VITE_API_BASE_URL=http://localhost:8090/api/v1 \
                          --build-arg VITE_KEYCLOAK_URL=http://localhost:8080 \
                          --build-arg VITE_KEYCLOAK_REALM=sisgr-realm \
                          --build-arg VITE_KEYCLOAK_CLIENT_ID=sisgr-frontend \
                          -t conyviera/proyectotingeso-frontend:latest .
                    '''
                }
            }
        }

        stage('Push to DockerHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push conyviera/proyectotingeso-backend:latest
                        docker push conyviera/proyectotingeso-frontend:latest
                    '''
                }
            }
        }
    }
}
